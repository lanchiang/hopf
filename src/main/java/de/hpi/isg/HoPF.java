package de.hpi.isg;

import com.google.common.collect.Sets;
import de.hpi.isg.constraints.ColumnStatistics;
import de.hpi.isg.feature.foreignkey.AttributeNameSimilarity;
import de.hpi.isg.feature.foreignkey.DataDistributionSimilarity;
import de.hpi.isg.feature.foreignkey.ForeignKeyFeature;
import de.hpi.isg.feature.primarykey.*;
import de.hpi.isg.pruning.ConflictRules;
import de.hpi.isg.pruning.ForeignKeyPruningRules;
import de.hpi.isg.target.Column;
import de.hpi.isg.target.InclusionDependencyInstance;
import de.hpi.isg.target.Table;
import de.hpi.isg.target.UniqueColumnCombinationInstance;
import de.hpi.isg.util.StringUtils;

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
    private DataProfiles dataProfiles; //Todo: need to instantiate

    /**
     * The predicted primary keys and foreign keys.
     */
    private Result result;

    private Set<PrimaryKeyFeature> primaryKeyFeatures;

    private Set<ForeignKeyFeature> foreignKeyFeatures;

    public HoPF() {
        dataProfiles = new DataProfiles();
    }

    /**
     * Execute the workflow of the {@link HoPF} algorithm.
     *
     */
    public void execute() {
        // load data profiles
        loadDataProfiles();

        // first prepare the data
        loadFeatures();
        calScore();

        // run the algorithm
        run();

        // cleanup the execution?
    }

    private void run() {
        // get all pk combination candidates
        Set<List<UniqueColumnCombinationInstance>> pkcCandidates = createPrimaryKeyCombinationCandidates(getUccInstancesByTable());

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
            ConflictRules conflictRules = new ConflictRules(null);
            // Todo: Define the matrix for connectivity

            List<InclusionDependencyInstance> discarded = new LinkedList<>();

            // create predicted foreign key list
            while (!remainInclusionDependencies.isEmpty()) {
                InclusionDependencyInstance foreignKeyInstanceCandidate = remainInclusionDependencies.get(0);
                if (!conflictRules.circleConflict(foreignKeyInstanceCandidate)) {
                    if (!ForeignKeyPruningRules.isPKBelongsToPK(foreignKeyInstanceCandidate)) {
                        predictedForeignKeys.add(foreignKeyInstanceCandidate);
                        conflictRules.addEdgeToColumnReferenceGraph(foreignKeyInstanceCandidate);
                        if (conflictRules.getTableReferenceGraph().isConnected()) {
                            conflictRules.getTableReferenceGraph().removeEdgeToTableReferenceGraph(foreignKeyInstanceCandidate);
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

    private void loadFeatures() {
        Map<Table, Set<UniqueColumnCombinationInstance>> uccInstancesByTable = getUccInstancesByTable();
        List<Column> columns = createColumns(dataProfiles.getColumnStatistics());

        // load primary key features
        this.primaryKeyFeatures = new HashSet<>();

        this.primaryKeyFeatures.add(new AttributeCardinality());
        this.primaryKeyFeatures.add(new AttributeValueLength(dataProfiles.getColumnStatistics())
        );
        this.primaryKeyFeatures.add(new AttributePosition(columns, uccInstancesByTable));
        this.primaryKeyFeatures.add(new NameSuffix());

        // load foreign key features
        this.foreignKeyFeatures = new HashSet<>();
        this.foreignKeyFeatures.add(new AttributeNameSimilarity(columns));
        this.foreignKeyFeatures.add(new DataDistributionSimilarity(indInstances, DataProfiles.DATA_PATH));
    }

    /**
     * Calculate the feature score for each of the UCCs and INDs.
     */
    private void calScore() {
        // calculate UCC feature scores
        primaryKeyFeatures.forEach(primaryKeyFeature -> primaryKeyFeature.score(uccInstances));
        uccInstances.forEach(uccInstance -> {
            double score = uccInstance.getFeatureScores().values().stream().mapToDouble(d -> d).sum() / (double) uccInstance.getFeatureScores().size();
            uccInstance.setScore(score);
        });

        // calculate IND feature scores
        foreignKeyFeatures.forEach(foreignKeyFeature -> foreignKeyFeature.score(indInstances));
        indInstances.forEach(indInstance -> {
            double score = indInstance.getFeatureScores().values().stream().mapToDouble(d -> d).sum() / (double) indInstance.getFeatureScores().size();
            indInstance.setScore(score);
        });
    }

    private Map<Table, Set<UniqueColumnCombinationInstance>> getUccInstancesByTable() {
        return uccInstances.stream().collect(
                Collectors.groupingBy(
                        UniqueColumnCombinationInstance::getBelongedTable,
                        Collectors.mapping(Function.identity(), Collectors.toSet())
                )
        );
    }

    private List<Column> createColumns(Set<ColumnStatistics> columnStatistics) {
        return columnStatistics.stream().map(colStat -> new Column(colStat.getColumnId(), colStat.getColumnName(), colStat.getTableId())).collect(Collectors.toList());
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

        Result globalBestResult = this.result;
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
}
