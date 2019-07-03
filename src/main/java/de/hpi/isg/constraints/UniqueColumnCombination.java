package de.hpi.isg.constraints;

import de.hpi.isg.target.ColumnCombination;
import de.hpi.isg.util.ReferenceUtils;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Lan Jiang
 */
public class UniqueColumnCombination implements Constraint {

    private final ColumnCombination columnCombination;

    public UniqueColumnCombination(int[] columnIds) {
        Validate.isTrue(ReferenceUtils.isSorted(columnIds));
        this.columnCombination = new ColumnCombination(columnIds);
    }

    public ColumnCombination getColumnCombination() {
        return columnCombination;
    }

    public int getArity() { return this.columnCombination.getColumnIds().length; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UniqueColumnCombination that = (UniqueColumnCombination) o;
        return columnCombination.equals(that.columnCombination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(columnCombination);
    }
}
