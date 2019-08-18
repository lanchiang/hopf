package de.hpi.isg.constraints;

import de.hpi.isg.element.ColumnCombination;
import de.hpi.isg.element.Table;
import de.hpi.isg.util.ReferenceUtils;
import lombok.Getter;
import org.apache.commons.lang3.Validate;

import java.util.Objects;

/**
 * @author Lan Jiang
 */
public class UniqueColumnCombination implements Constraint {

    @Getter
    private final ColumnCombination columnCombination;

    @Getter
    private final int tableId;

    public UniqueColumnCombination(int tableId, int... columnIds) {
        Validate.isTrue(ReferenceUtils.isSorted(columnIds));
        this.columnCombination = new ColumnCombination(columnIds);

        this.tableId = tableId;
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
