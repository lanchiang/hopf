package de.hpi.isg.evaluation;

import de.hpi.isg.Result;
import de.hpi.isg.constraints.Constraint;
import de.hpi.isg.constraints.InclusionDependency;
import de.hpi.isg.constraints.UniqueColumnCombination;
import de.hpi.isg.element.InclusionDependencyInstance;
import de.hpi.isg.element.UniqueColumnCombinationInstance;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author lan
 * @since 2021/8/2
 */
public class Evaluator {

    public void evaluate(Result result, Collection<? extends Constraint> primaryKeys, Collection<? extends Constraint> foreignKeys) {
        Collection<UniqueColumnCombination> predictedPrimaryKeys = result.getPrimaryKeys().stream()
                .map(UniqueColumnCombinationInstance::getUcc).collect(Collectors.toSet());
        Collection<InclusionDependency> predictedForeignKeys = result.getForeignKeys().stream()
                .map(InclusionDependencyInstance::getInd).collect(Collectors.toSet());

        System.out.println(predictedPrimaryKeys.size() + "\t" + predictedForeignKeys.size());

        // true positive primary key instances
        Collection<UniqueColumnCombination> tpPKInstances = primaryKeys.stream().filter(constraint -> {
            UniqueColumnCombination primaryKey = (UniqueColumnCombination) constraint;
            return predictedPrimaryKeys.contains(primaryKey);
        }).map(constraint -> (UniqueColumnCombination) constraint).collect(Collectors.toSet());

        System.out.println(tpPKInstances.size());

        // true positive instances
        Collection<InclusionDependency> tpFKInstances = foreignKeys.stream().filter(constraint -> {
            InclusionDependency foreignKey = (InclusionDependency) constraint;
            return predictedForeignKeys.contains(foreignKey);
        }).map(constraint -> (InclusionDependency) constraint).collect(Collectors.toSet());

        System.out.println(tpFKInstances.size());

        //
        return;
    }
}
