package de.hpi.isg.pruning;

import de.hpi.isg.constraints.InclusionDependency;
import de.hpi.isg.constraints.UniqueColumnCombination;
import de.hpi.isg.target.InclusionDependencyInstance;
import de.hpi.isg.target.UniqueColumnCombinationInstance;

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
     * @param primaryKeys the primary key set.
     * @return the inclusion dependencies whose RHS are among the primary key set.
     */
    public static List<InclusionDependencyInstance> referenceToPrimaryKey(Set<InclusionDependencyInstance> inclusionDependencyInstances,
                                                                          List<UniqueColumnCombinationInstance> primaryKeys) {
        List<InclusionDependencyInstance> foreignKeyCandidates = inclusionDependencyInstances.stream()
                .filter(inclusionDependencyInstance -> {
                    InclusionDependency inclusionDependency = inclusionDependencyInstance.getInd();
                    int[] refColumnIds = inclusionDependency.getRhs().getColumnIds();
                    UniqueColumnCombination rhs = new UniqueColumnCombination(refColumnIds);
                    return primaryKeys.stream().map(UniqueColumnCombinationInstance::getUcc).anyMatch(ucc -> ucc.equals(rhs));
                }).collect(Collectors.toList());
        return foreignKeyCandidates;
    }

    // Todo
    public static boolean isPKBelongsToPK(InclusionDependencyInstance inclusionDependencyInstance) {
        InclusionDependency inclusionDependency = inclusionDependencyInstance.getInd();
        int[] depColumnIds = inclusionDependency.getLhs().getColumnIds();
        int[] refColumnIds = inclusionDependency.getRhs().getColumnIds();
        return false;
    }
}
