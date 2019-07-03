package de.hpi.isg.feature.foreignkey;

import de.hpi.isg.DataProfiles;
import de.hpi.isg.binning.Binning;
import de.hpi.isg.binning.BinningUtils;
import de.hpi.isg.binning.EqualWidthBinning;
import de.hpi.isg.constraints.InclusionDependency;
import de.hpi.isg.element.ColumnCombination;
import de.hpi.isg.element.InclusionDependencyInstance;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Lan Jiang
 */
public class DataDistributionSimilarity extends ForeignKeyFeature {

    private Map<InclusionDependency, Double> unaryScore;

    public DataDistributionSimilarity(Set<InclusionDependencyInstance> indInstances, String dataPath) {
        unaryScore = new HashMap<>();

        Set<InclusionDependency> inclusionDependencies = indInstances.stream().map(InclusionDependencyInstance::getInd).collect(Collectors.toSet());
        Set<InclusionDependency> unaryInclusionDependencies = inclusionDependencies.stream()
                .flatMap(this::splitIntoUnaryInclusionDependencies)
                .collect(Collectors.toSet());

        List<Integer> refColumnIds = unaryInclusionDependencies.stream().map(ind -> ind.getRhs().getColumnIds()[0]).collect(Collectors.toList());

        Map<Integer, Binning> refColumnBinning = new HashMap<>();
        for (int refColumnId : refColumnIds) {
            List<Object> data = new LinkedList<>();

            Binning binning = new EqualWidthBinning(data, DataProfiles.BIN_NUMBER);
            binning.assignOwnData();
            refColumnBinning.putIfAbsent(refColumnId, binning);
        }

        Map<ColumnCombination, Set<InclusionDependency>> unaryIndsByLhs = unaryInclusionDependencies.stream()
                .collect(Collectors.groupingBy(InclusionDependency::getLhs, Collectors.toSet()));
        unaryIndsByLhs.forEach((columnCombination, inds) -> {
            List<Object> lhsData = new LinkedList<>();

            inds.forEach(ind -> {
                Binning baseBins = refColumnBinning.get(ind.getRhs().getColumnIds()[0]);
                Binning.Bin[] guestBins = baseBins.assignData(lhsData);
                unaryScore.putIfAbsent(ind, BinningUtils.binningSimilarity(baseBins.getBins(), guestBins));
            });
        });
    }

    @Override
    public void score(Collection<InclusionDependencyInstance> indInstances) {
        for (InclusionDependencyInstance indInstance : indInstances) {
            InclusionDependency inclusionDependency = indInstance.getInd();
            int[] depColumnIds = inclusionDependency.getLhs().getColumnIds();
            int[] refColumnIds = inclusionDependency.getRhs().getColumnIds();
            double score = 0;
            for (int i=0;i<depColumnIds.length;i++) {
                score += unaryScore.get(new InclusionDependency(depColumnIds[i], refColumnIds[i]));
            }
            score /= (double)depColumnIds.length;

            indInstance.getFeatureScores().putIfAbsent(getClass().getSimpleName(), score);
        }

        normalize(indInstances);
    }

    /**
     * Splits a given {@link InclusionDependency} into a set of unary {@link InclusionDependency}s.
     *
     * @param inclusionDependency the {@link InclusionDependency} to split
     * @return the unary {@link InclusionDependency}s
     */
    private Stream<InclusionDependency> splitIntoUnaryInclusionDependencies(InclusionDependency inclusionDependency) {
        List<InclusionDependency> unaryInclusionDependencies = new ArrayList<>(inclusionDependency.getArity());
        final int[] depColumnIds = inclusionDependency.getLhs().getColumnIds();
        final int[] refColumnIds = inclusionDependency.getRhs().getColumnIds();
        for (int i = 0; i < inclusionDependency.getArity(); i++) {
            unaryInclusionDependencies.add(new InclusionDependency(depColumnIds[i], refColumnIds[i]));
        }
        return unaryInclusionDependencies.stream();
    }
}
