package de.hpi.isg.apps;

import com.google.common.collect.Sets;
import de.hpi.isg.DataProfiles;
import de.hpi.isg.Result;
import de.hpi.isg.constraints.ColumnStatistics;
import de.hpi.isg.constraints.InclusionDependency;
import de.hpi.isg.evaluation.Evaluator;
import de.hpi.isg.feature.foreignkey.AttributeNameSimilarity;
import de.hpi.isg.feature.foreignkey.DataDistributionSimilarity;
import de.hpi.isg.feature.foreignkey.ForeignKeyFeature;
import de.hpi.isg.feature.primarykey.*;
import de.hpi.isg.pruning.ConflictDetector;
import de.hpi.isg.pruning.ForeignKeyPruningRules;
import de.hpi.isg.element.Column;
import de.hpi.isg.element.InclusionDependencyInstance;
import de.hpi.isg.element.Table;
import de.hpi.isg.element.UniqueColumnCombinationInstance;
import de.hpi.isg.util.CSVSlicer;
import de.hpi.isg.util.JCommanderParser;
import de.hpi.isg.util.Parameters;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Lan Jiang
 */
public class HoPF {

    /**
     * All UCCs of the input dataset.
     */
    private Set<UniqueColumnCombinationInstance> uccInstances;

    /**
     * All INDs of the input dataset.
     */
    private Set<InclusionDependencyInstance> indInstances;

    /**
     * The profiles of the input dataset.
     */
    private DataProfiles dataProfiles;

    /**
     * Map from relation.column name to the non-empty value list of it.
     */
    private Map<Integer, List<String>> dataSlices;

    /**
     * The predicted primary keys and foreign keys.
     * Todo: This should not be a member variable of this class
     */
    private Result result;

    private Set<PrimaryKeyFeature> primaryKeyFeatures;

    private Set<ForeignKeyFeature> foreignKeyFeatures;

    private final Parameters parameters;

    public HoPF(Parameters parameters) {
        this.parameters = parameters;
        this.dataProfiles = new DataProfiles(this.parameters.getProfileBasicPath());
    }

    /**
     * Execute the workflow of the {@link HoPF} algorithm.
     */
    public void execute() {
        // load data profiles into main memory
//        DataProfileReader dataProfileReader = new DataProfileReader(this.parameters.getProfileBasicPath());
//        DataProfiles dataProfiles = dataProfileReader.loadDataProfiles();

        // first prepare the data
        prepareData();

        loadFeatures();

        calculateCandidateScore();

        // run the algorithm
        run();
        int placeholder = 0;

        // evaluate
        if (this.parameters.isEvaluate()) {
            Evaluator evaluator = new Evaluator();
            evaluator.evaluate(this.result, this.dataProfiles.getPrimaryKeys(), this.dataProfiles.getForeignKeys());
        } else {

        }
    }

    private void prepareData() {
        // load data profiles
        loadDataProfiles();

        // load data slices
        loadDataSlices();

//        indInstances = indInstances.stream().filter(indInstance -> {
//            InclusionDependency ind = indInstance.getInd();
//            long lhsEmptyColumnCount =
//                    dataProfiles.getColumnStatistics().stream()
//                            .filter(cs -> ArrayUtils.contains(ind.getLhs().getColumnIds(), cs.getColumnId()))
//                            .filter(cs -> cs.getColumnValues().size() == 0).count();
//            if (lhsEmptyColumnCount > 0) {
//                return false;
//            }
//            long rhsEmptyColumnCount =
//                    dataProfiles.getColumnStatistics().stream()
//                            .filter(cs -> ArrayUtils.contains(ind.getRhs().getColumnIds(), cs.getColumnId()))
//                            .filter(cs -> cs.getColumnValues().size() == 0).count();
//            if (rhsEmptyColumnCount > 0) {
//                return false;
//            }
//            return true;
//        }).collect(Collectors.toSet());

        indInstances = indInstances.stream().filter(indInstance -> {
            InclusionDependency ind = indInstance.getInd();
            return Arrays.stream(ind.getLhs().getColumnIds()).allMatch(id -> dataSlices.get(id).size() != 0)
                    && Arrays.stream(ind.getRhs().getColumnIds()).allMatch(id -> dataSlices.get(id).size() != 0);
        }).collect(Collectors.toSet());

        return;
    }

    /**
     * Construct primary key search space by calculating the Cartesian product of the sets of primary key candidates of all tables.
     * Primary key candidate list of every table pruned by the cliff metric.
     *
     * @param uccInstancesByTable
     * @return
     */
    private Set<List<UniqueColumnCombinationInstance>> createPrimaryKeyCombCandidates(
            Map<Table, List<UniqueColumnCombinationInstance>> uccInstancesByTable) {
        uccInstancesByTable.forEach((key, value) -> uccInstancesByTable.put(key, value.stream().sorted(Comparator.comparingDouble(UniqueColumnCombinationInstance::getScore).reversed()).collect(Collectors.toList())));
        Map<Table, List<UniqueColumnCombinationInstance>> cut = pruneByCliff(uccInstancesByTable);
        Set<List<UniqueColumnCombinationInstance>> pkcCandidates = new HashSet<>();
        List<Table> tables = new ArrayList<>(cut.keySet());
        bruteForcePrimaryKey(cut, tables, new ArrayList<>(), 0, pkcCandidates);
        return pkcCandidates;
    }

    private void bruteForcePrimaryKey(Map<Table, List<UniqueColumnCombinationInstance>> uccInstancesByTable,
                                      List<Table> tables,
                                      List<UniqueColumnCombinationInstance> pathPK,
                                      int depth,
                                      Set<List<UniqueColumnCombinationInstance>> pkcCandidates) {
        if (depth >= tables.size())
            return;
        for (UniqueColumnCombinationInstance uccInstance : uccInstancesByTable.get(tables.get(depth))) {
            List<UniqueColumnCombinationInstance> currentPath = new ArrayList<>(pathPK);
            currentPath.add(uccInstance);
            bruteForcePrimaryKey(uccInstancesByTable, tables, currentPath, depth + 1, pkcCandidates);
            if (depth == tables.size() - 1) {
                pkcCandidates.add(currentPath);
            }
        }
    }

    private void run() {
        // get all pk combination candidates
        Set<List<UniqueColumnCombinationInstance>> pkcCandidates = createPrimaryKeyCombCandidates(getUccInstancesByTable());

        // treat each primary key candidate combinations as the true primary keys.
        for (List<UniqueColumnCombinationInstance> primaryKeys : pkcCandidates) {
            List<InclusionDependencyInstance> foreignKeyCandidates = ForeignKeyPruningRules.referenceToPrimaryKey(indInstances, primaryKeys);

            // create an ordered foreign key candidate list that is sorted by its score
            List<InclusionDependencyInstance> scoreOrderedIndInstances = foreignKeyCandidates.stream()
                    .sorted(Comparator.comparingDouble(InclusionDependencyInstance::getScore).reversed())
                    .collect(Collectors.toList());

            List<InclusionDependencyInstance> predictedForeignKeys = new LinkedList<>();
            List<InclusionDependencyInstance> remainInclusionDependencies = new ArrayList<>(scoreOrderedIndInstances);

            // Define the conflict rules
            ConflictDetector conflictDetector = new ConflictDetector(dataProfiles.getColumnStatistics());

            List<InclusionDependencyInstance> discarded = new LinkedList<>();

            // create predicted foreign key list
            while (!remainInclusionDependencies.isEmpty()) {
                InclusionDependencyInstance foreignKeyInstanceCandidate = remainInclusionDependencies.get(0);
                if (!conflictDetector.detectCyclicReference(foreignKeyInstanceCandidate)) {
                    if (!ForeignKeyPruningRules.isPKBelongsToPK(foreignKeyInstanceCandidate, primaryKeys, dataProfiles)) {
                        predictedForeignKeys.add(foreignKeyInstanceCandidate);
                        conflictDetector.addEdgeToColumnReferenceGraph(foreignKeyInstanceCandidate);
                        if (conflictDetector.isSchemaConnected(foreignKeyInstanceCandidate)) {
                            conflictDetector.removeEdgeFromTableReferenceGraph(foreignKeyInstanceCandidate);
                            break;
                        }
                    } else {
                        discarded.add(foreignKeyInstanceCandidate);
                    }
                }

                // remove the fk candidates that share the same lhs with the current inspected one. Implementation of the conflict rule 'uniqueness of foreign keys'
                remainInclusionDependencies = remainInclusionDependencies.stream()
                        .filter(indInstance -> !indInstance.getInd().getLhs().equals(foreignKeyInstanceCandidate.getInd().getLhs()))
                        .collect(Collectors.toList());
            }

            Result result = new Result(primaryKeys, predictedForeignKeys);
            this.result = updateResults(reduceResults(result, discarded));
        }
    }

    private void loadDataProfiles() {
        dataProfiles.readConstraints();
        uccInstances = dataProfiles.getUccs().stream().map(UniqueColumnCombinationInstance::new).collect(Collectors.toSet());
        indInstances = dataProfiles.getInds().stream().map(InclusionDependencyInstance::new).collect(Collectors.toSet());
    }

    private void loadDataSlices() {
        List<File> files = Arrays.stream(Objects.requireNonNull(new File(this.parameters.getDataPath()).listFiles()))
                .filter(file -> !file.getName().equals(".DS_Store")).collect(Collectors.toList());
        dataSlices = new HashMap<>();
        files.forEach(file -> {
            CSVSlicer csvSlicer = new CSVSlicer(file, this.parameters);
            Map<String, List<String>> partialDataSlices = csvSlicer.generateDataSlices();
            partialDataSlices.forEach((key, value) -> {
                String[] splits = key.split("\\.");
                String tableName = splits[0];
                String columnName = splits[1];
                int columnId = this.dataProfiles.getStatByTableAndColumnName(tableName, columnName).getColumnId();
                dataSlices.putIfAbsent(columnId, value);
            });
        });
    }

    private void loadFeatures() {
        Map<Table, List<UniqueColumnCombinationInstance>> uccInstancesByTable = getUccInstancesByTable();
        List<Column> columns = createColumns(dataProfiles.getColumnStatistics());

        // load primary key features
        this.primaryKeyFeatures = new HashSet<>();

        this.primaryKeyFeatures.add(new AttributeCardinality());
        this.primaryKeyFeatures.add(new AttributeValueLength(dataProfiles.getColumnStatistics())
        );
        this.primaryKeyFeatures.add(new AttributePosition(columns, uccInstancesByTable));
        this.primaryKeyFeatures.add(new NameSuffix(columns));

        // load foreign key features
        this.foreignKeyFeatures = new HashSet<>();
        this.foreignKeyFeatures.add(new AttributeNameSimilarity(columns));
        this.foreignKeyFeatures.add(new DataDistributionSimilarity(indInstances, dataSlices, this.parameters));
    }

    /**
     * Calculate the feature score for each UCC and IND.
     */
    private void calculateCandidateScore() {
        // calculate UCC feature scores
        primaryKeyFeatures.forEach(primaryKeyFeature -> primaryKeyFeature.score(uccInstances));
        uccInstances.forEach(uccInstance -> {
            double score = uccInstance.getFeatureScores().values().stream().mapToDouble(object -> Double.parseDouble(object.toString())).sum() / (double) uccInstance.getFeatureScores().size();
            uccInstance.setScore(score);
        });

        // calculate IND feature scores
        foreignKeyFeatures.forEach(foreignKeyFeature -> foreignKeyFeature.score(indInstances));
        indInstances.forEach(indInstance -> {
            double score = indInstance.getFeatureScores().values().stream().mapToDouble(object -> Double.parseDouble(object.toString())).sum() / (double) indInstance.getFeatureScores().size();
            indInstance.setScore(score);
        });
    }

    private Map<Table, List<UniqueColumnCombinationInstance>> getUccInstancesByTable() {
        Map<Table, List<UniqueColumnCombinationInstance>> uccInstancesByTable = uccInstances.stream().collect(
                Collectors.groupingBy(
                        UniqueColumnCombinationInstance::getBelongedTable,
                        Collectors.mapping(Function.identity(), Collectors.toList())
                )
        );
        return uccInstancesByTable;
    }

    private Map<Table, List<UniqueColumnCombinationInstance>> pruneByCliff(Map<Table, List<UniqueColumnCombinationInstance>> uccInstancesByTable) {
        final double eps = 0d;
        double cliff;
        int count;
        for (Iterator<Map.Entry<Table, List<UniqueColumnCombinationInstance>>> iterator = uccInstancesByTable.entrySet().iterator();
             iterator.hasNext(); ) {
            cliff = eps;
            count = 0;
            Map.Entry<Table, List<UniqueColumnCombinationInstance>> next = iterator.next();
            List<UniqueColumnCombinationInstance> scoreMap = next.getValue();
            if (scoreMap.size() < 2) {
                continue;
            }
            List<Double> scores = scoreMap.stream().map(UniqueColumnCombinationInstance::getScore).collect(Collectors.toList());
            for (int i = 1; i < scores.size(); i++) {
                if (scores.get(i - 1) - scores.get(i) > cliff) {
                    cliff = scores.get(i - 1) - scores.get(i);
                    count = i;
                }
            }

            if (cliff == 0) {
                uccInstancesByTable.put(next.getKey(), next.getValue());
                continue;
            }
            List<UniqueColumnCombinationInstance> newScoreMap = new LinkedList<>();
            int i = 0;
            for (Iterator<UniqueColumnCombinationInstance> scoreIterator = scoreMap.iterator(); scoreIterator.hasNext(); ) {
                if (i == count) {
                    break;
                }
                UniqueColumnCombinationInstance nextScore = scoreIterator.next();
                newScoreMap.add(nextScore);
                i++;
            }
            uccInstancesByTable.put(next.getKey(), newScoreMap);
        }
        return uccInstancesByTable;
    }

    private List<Column> createColumns(Set<ColumnStatistics> columnStatistics) {
        return columnStatistics.stream().map(colStat ->
                new Column(colStat.getColumnId(), colStat.getColumnName(), colStat.getTableId(), colStat.getTableName()))
                .collect(Collectors.toList());
    }

    /**
     * Create all primary key combination candidates. A primary key combination is a list of {@link UniqueColumnCombinationInstance}s whose elements are UCCs
     * from different tables. (exactly once from each table).
     *
     * @param uccInstancesByTable the table to its UCC list map.
     * @return the set of all primary key combination candidates.
     */
    private Set<List<UniqueColumnCombinationInstance>> createPrimaryKeyCombinationCandidates(
            Map<Table, Set<UniqueColumnCombinationInstance>> uccInstancesByTable) {
        return Sets.cartesianProduct(new ArrayList<>(uccInstancesByTable.values()));
    }

    /**
     * Update the predicted primary keys and foreign keys by employing pk reducing operation.
     *
     * @param result the reduced primary keys and foreign keys.
     * @return the updated predicted primary keys and foreign keys.
     */
    private Result updateResults(Result result) {
        double pkReducedScore = result.getPrimaryKeys().stream().mapToDouble(UniqueColumnCombinationInstance::getScore).sum() / (double) result.getPrimaryKeys().size();
        double fkGlobalScore;
        List<InclusionDependencyInstance> foreignKeys;

        Result globalBestResult = (this.result != null) ? this.result : result;
        double pkGlobalScore = globalBestResult.getPrimaryKeys().stream().mapToDouble(UniqueColumnCombinationInstance::getScore).sum() / (double) globalBestResult.getPrimaryKeys().size();

        double fkReducedScore;
        if (globalBestResult.getForeignKeys().size() > result.getForeignKeys().size()) {
            foreignKeys = globalBestResult.getForeignKeys().subList(0, result.getForeignKeys().size());
            fkReducedScore = result.getForeignKeys().stream().mapToDouble(InclusionDependencyInstance::getScore).sum() / (double) result.getForeignKeys().size();
            fkGlobalScore = foreignKeys.stream().mapToDouble(InclusionDependencyInstance::getScore).sum() / (double) foreignKeys.size();
        } else {
            foreignKeys = result.getForeignKeys().subList(0, globalBestResult.getForeignKeys().size());
            fkReducedScore = foreignKeys.stream().mapToDouble(InclusionDependencyInstance::getScore).sum() / (double) foreignKeys.size();
            fkGlobalScore = globalBestResult.getForeignKeys().stream().mapToDouble(InclusionDependencyInstance::getScore).sum() / (double) globalBestResult.getForeignKeys().size();
        }
        double globalScore = pkGlobalScore + fkGlobalScore;
        if (globalScore < pkReducedScore + fkReducedScore) {
            return result;
        } else {
            return globalBestResult;
        }
    }

    /**
     * Remove the predicted primary keys and add the corresponding foreign keys (reduce), if this can increase the overall score.
     * The corresponding foreign keys are those whose LHS equals to the inspected primary key.
     *
     * @param result    the predicted primary keys and foreign keys.
     * @param discarded the foreign key candidates that fulfill the condition: 'Pk belongs to Pk'
     * @return the reduced primary keys and foreign keys.
     */
    private Result reduceResults(Result result,
                                 List<InclusionDependencyInstance> discarded) {
        List<InclusionDependencyInstance> foreignKeyResult = result.getForeignKeys();
        List<UniqueColumnCombinationInstance> primaryKeys = new ArrayList<>(result.getPrimaryKeys());

        for (Iterator<UniqueColumnCombinationInstance> pkIterator = primaryKeys.iterator(); pkIterator.hasNext(); ) {
            UniqueColumnCombinationInstance primaryKey = pkIterator.next();
            List<InclusionDependencyInstance> reducedForeignKeyResult = foreignKeyResult.stream()
                    .filter(indInstance -> !indInstance.getInd().getRhs().equals(primaryKey.getUcc().getColumnCombination()))
                    .collect(Collectors.toList());
            List<InclusionDependencyInstance> discardedInds = discarded.stream()
                    .filter(indInstance -> indInstance.getInd().getLhs().equals(primaryKey.getUcc().getColumnCombination()))
                    .collect(Collectors.toList());
            if (discardedInds.isEmpty()) {
                continue;
            }
            reducedForeignKeyResult.addAll(discardedInds);
            if (shouldReduce(result.getPrimaryKeys(), primaryKey, foreignKeyResult, reducedForeignKeyResult)) {
                pkIterator.remove();
                foreignKeyResult = reducedForeignKeyResult;
            }
        }

        return new Result(primaryKeys, foreignKeyResult);
    }

    /**
     * Check whether removing the inspected primary key and adding the corresponding foreign keys will reduce the overall score.
     * The corresponding foreign keys are those whose LHS equals to the inspected primary key.
     *
     * @param predictedPrimaryKeys all the predicted primary keys.
     * @param predictedPrimaryKey  the inspected primary key.
     * @param predictedForeignKeys the original foreign key result.
     * @param reducedForeignKeys   the reduced foreign key result.
     * @return true if the inspected primary key should be removed.
     */
    private boolean shouldReduce(List<UniqueColumnCombinationInstance> predictedPrimaryKeys,
                                 UniqueColumnCombinationInstance predictedPrimaryKey,
                                 List<InclusionDependencyInstance> predictedForeignKeys,
                                 List<InclusionDependencyInstance> reducedForeignKeys) {
        double pkOriginalScore = predictedPrimaryKeys.stream()
                .mapToDouble(UniqueColumnCombinationInstance::getScore).sum() / (double) predictedPrimaryKeys.size();
        double pkReducedScore = predictedPrimaryKeys.stream().filter(ucc -> !ucc.equals(predictedPrimaryKey))
                .mapToDouble(UniqueColumnCombinationInstance::getScore).sum() / (double) (predictedPrimaryKeys.size() - 1);

        List<InclusionDependencyInstance> foreignKeys;
        double fkReducedScore, fkOriginalScore;

        if (predictedForeignKeys.size() > reducedForeignKeys.size()) {
            foreignKeys = predictedForeignKeys.subList(0, reducedForeignKeys.size());
            fkReducedScore = reducedForeignKeys.stream().mapToDouble(InclusionDependencyInstance::getScore).sum() / (double) foreignKeys.size();
            fkOriginalScore = foreignKeys.stream().mapToDouble(InclusionDependencyInstance::getScore).sum() / (double) foreignKeys.size();
        } else {
            foreignKeys = reducedForeignKeys.subList(0, predictedForeignKeys.size());
            fkReducedScore = foreignKeys.stream().mapToDouble(InclusionDependencyInstance::getScore).sum() / (double) foreignKeys.size();
            fkOriginalScore = predictedForeignKeys.stream().mapToDouble(InclusionDependencyInstance::getScore).sum() / (double) foreignKeys.size();
        }

        return pkOriginalScore + fkOriginalScore < pkReducedScore + fkReducedScore;
    }

    /**
     * Entry of the program.
     *
     * @param args command line parameters.
     */
    public static void main(String[] args) {
        Parameters parameters = new Parameters();
        JCommanderParser.parseCommandLineAndExitOnError(parameters, args);
        new HoPF(parameters).execute();
    }
}
