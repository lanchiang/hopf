package de.hpi.isg.feature.foreignkey;

import de.hpi.isg.constraints.InclusionDependency;
import de.hpi.isg.element.ColumnCombination;
import de.hpi.isg.element.InclusionDependencyInstance;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lan Jiang
 */
abstract public class ForeignKeyFeature {

    Map<InclusionDependency, Double> normalize(Map<ColumnCombination, Map<InclusionDependencyInstance, Double>> scores) {
        Map<InclusionDependency, Double> scoresByIND = new HashMap<>();
        for (ColumnCombination columnCombination : scores.keySet()) {
            Map<InclusionDependencyInstance, Double> scoresByLHS = scores.get(columnCombination);
//            double min = scoresByLHS.values().stream().mapToDouble(value -> value).min().getAsDouble();
//            double max = scoresByLHS.values().stream().mapToDouble(value -> value).max().getAsDouble();
            for (InclusionDependencyInstance inclusionDependency : scoresByLHS.keySet()) {
//                if (min==max) {
//                    scoresByLHS.put(inclusionDependency, min);
//                } else {
//                    scoresByLHS.put(inclusionDependency, (scoresByLHS.get(inclusionDependency)-min)/(max-min));
//                }
                scoresByIND.putIfAbsent(inclusionDependency.getInd(), scoresByLHS.get(inclusionDependency));
            }
        }
        return scoresByIND;
    }

    void normalize(Collection<InclusionDependencyInstance> indInstances) {

    }


    abstract public void score(Collection<InclusionDependencyInstance> indInstances);
}
