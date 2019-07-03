package de.hpi.isg.feature.primarykey;

import de.hpi.isg.constraints.UniqueColumnCombination;
import de.hpi.isg.target.UniqueColumnCombinationInstance;
import de.hpi.isg.target.Table;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lan Jiang
 */
abstract public class PrimaryKeyFeature {

    public Map<UniqueColumnCombination, Double> normalize(Map<Table, Map<UniqueColumnCombination, Double>> scores) {
        Map<UniqueColumnCombination, Double> scoresByUCC = new HashMap<>();
        for (Table table : scores.keySet()) {
            Map<UniqueColumnCombination, Double> scoresWithinTable = scores.get(table);
//            double min = scoresWithinTable.values().stream().mapToDouble(value -> value).min().getAsDouble();
//            double max = scoresWithinTable.values().stream().mapToDouble(value -> value).max().getAsDouble();
            for (UniqueColumnCombination uniqueColumnCombination : scoresWithinTable.keySet()) {
//                int[] columnIds = uniqueColumnCombination.getColumnIds();
//                if (min==max) {
//                    scoresWithinTable.put(uniqueColumnCombination, min);
//                } else {
//                    scoresWithinTable.put(uniqueColumnCombination, (scoresWithinTable.get(uniqueColumnCombination)-min)/(max-min));
//                }
                scoresByUCC.putIfAbsent(uniqueColumnCombination, scoresWithinTable.get(uniqueColumnCombination));
            }
        }
        return scoresByUCC;
    }

    void normalize(Collection<UniqueColumnCombinationInstance> uccInstances) {

    }

    abstract public void score(Collection<UniqueColumnCombinationInstance> uccInstances);
}
