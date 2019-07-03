package de.hpi.isg.element;

import de.hpi.isg.util.ReferenceUtils;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;

/**
 * @author Lan Jiang
 */
public class ColumnCombination {

    private final int[] columnIds;


    public ColumnCombination(int columnId, String columnName) {
        this(new int[]{columnId});
    }


    public ColumnCombination(int columnId) {
        this(new int[]{columnId});
    }

    public ColumnCombination(int[] columnIds) {
        Validate.isTrue(ReferenceUtils.isSorted(columnIds));
        this.columnIds = columnIds;
    }

    public int[] getColumnIds() {
        return columnIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnCombination that = (ColumnCombination) o;
        return Arrays.equals(columnIds, that.columnIds);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(columnIds);
    }
}