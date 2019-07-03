package de.hpi.isg.constraints;

import de.hpi.isg.element.ColumnCombination;
import org.apache.commons.lang3.Validate;

/**
 * @author Lan Jiang
 */
public class InclusionDependency implements Constraint {

    private final ColumnCombination lhs;

    private final ColumnCombination rhs;

    public InclusionDependency(int dependentColumnId, int referencedColumnId) {
        this(new int[]{dependentColumnId}, new int[]{referencedColumnId});
    }

    public InclusionDependency(int[] dependentColumnIds, int[] referencedColumnIds) {
        Validate.isTrue(dependentColumnIds.length == referencedColumnIds.length);
        this.lhs = new ColumnCombination(dependentColumnIds);
        this.rhs = new ColumnCombination(referencedColumnIds);
    }

    public ColumnCombination getLhs() {
        return lhs;
    }

    public ColumnCombination getRhs() {
        return rhs;
    }

    public int getArity() {
        return this.lhs.getColumnIds().length;
    }
}
