package de.hpi.isg.pruning;

import de.hpi.isg.DataProfiles;
import de.hpi.isg.constraints.ColumnStatistics;
import de.hpi.isg.constraints.InclusionDependency;
import de.hpi.isg.constraints.UniqueColumnCombination;
import de.hpi.isg.element.ColumnCombination;
import de.hpi.isg.element.InclusionDependencyInstance;
import de.hpi.isg.element.UniqueColumnCombinationInstance;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Lan Jiang
 */
public class ForeignKeyPruningRules {

    /**
     * Preserve only the inclusion dependencies whose RHS are among the primary key set.
     *
     * @param inclusionDependencyInstances the inclusion dependency set to be pruned.
     * @param primaryKeys                  the primary key set.
     * @return the inclusion dependencies whose RHS are among the primary key set.
     */
    public static List<InclusionDependencyInstance> referenceToPrimaryKey(Set<InclusionDependencyInstance> inclusionDependencyInstances,
                                                                          List<UniqueColumnCombinationInstance> primaryKeys) {
        return inclusionDependencyInstances.stream()
                .filter(inclusionDependencyInstance -> {
                    InclusionDependency inclusionDependency = inclusionDependencyInstance.getInd();
                    int[] refColumnIds = inclusionDependency.getRhs().getColumnIds();
                    UniqueColumnCombination rhs = new UniqueColumnCombination(inclusionDependency.getRhs().getTableId(), refColumnIds);
                    return primaryKeys.stream().map(UniqueColumnCombinationInstance::getUcc).anyMatch(ucc -> ucc.equals(rhs));
                }).collect(Collectors.toList());
    }

    public static boolean isPKBelongsToPK(InclusionDependencyInstance inclusionDependencyInstance,
                                          List<UniqueColumnCombinationInstance> primaryKeys,
                                          DataProfiles dataProfiles) {
        InclusionDependency inclusionDependency = inclusionDependencyInstance.getInd();
        int[] depColumnIds = inclusionDependency.getLhs().getColumnIds();
        int[] refColumnIds = inclusionDependency.getRhs().getColumnIds();
        if (depColumnIds.length > 1) {
            return false;
        }

        ColumnStatistics depColumnStat = dataProfiles.getColumnStatistics()
                .stream().filter(cs -> cs.getColumnId() == depColumnIds[0]).findFirst().get();
        ColumnStatistics refColumnStat = dataProfiles.getColumnStatistics()
                .stream().filter(cs -> cs.getColumnId() == refColumnIds[0]).findFirst().get();
        if (!(depColumnStat.getType().contains("INT") && refColumnStat.getType().contains("INT"))) {
            return false;
        }

        double depEntropy = dataProfiles.getColumnStatistics().stream().filter(cs -> cs.getColumnId() == depColumnIds[0])
                .map(ColumnStatistics::getEntropy).findFirst().get();
        double refEntropy = dataProfiles.getColumnStatistics().stream().filter(cs -> cs.getColumnId() == refColumnIds[0])
                .map(ColumnStatistics::getEntropy).findFirst().get();

        boolean lhsIsPrimaryKey = primaryKeys.stream()
                .map(cs -> cs.getUcc().getColumnCombination().getColumnIds())
                .anyMatch(columnIds -> Arrays.equals(columnIds, depColumnIds));
        return lhsIsPrimaryKey && depEntropy <= refEntropy;
    }
}
