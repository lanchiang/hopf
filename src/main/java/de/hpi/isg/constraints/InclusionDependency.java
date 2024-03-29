package de.hpi.isg.constraints;

import de.hpi.isg.element.ColumnCombination;
import de.hpi.isg.util.ReferenceUtils;
import org.apache.commons.lang3.Validate;

import java.util.Objects;

/**
 * @author Lan Jiang
 */
public class InclusionDependency implements Constraint {

    private final ColumnCombination lhs;

    private final ColumnCombination rhs;

    public InclusionDependency(final int dependentColumnId, final int depTableId,
                               final int referencedColumnId, final int refTableId) {
        this(new int[]{dependentColumnId}, depTableId, new int[]{referencedColumnId}, refTableId);
    }

    public InclusionDependency(final int[] dependentColumnIds, final int depTableId,
                               final int[] referencedColumnIds, final int refTableId) {
        Validate.isTrue(dependentColumnIds.length == referencedColumnIds.length);
        Validate.isTrue(ReferenceUtils.isSorted(dependentColumnIds));
        this.lhs = new ColumnCombination(dependentColumnIds, depTableId);
        this.rhs = new ColumnCombination(referencedColumnIds, refTableId);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InclusionDependency that = (InclusionDependency) o;
        return Objects.equals(lhs, that.lhs) &&
                Objects.equals(rhs, that.rhs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lhs, rhs);
    }
}
