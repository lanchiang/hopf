package de.hpi.isg.element;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Lan Jiang
 */
public class ColumnCombination {

    @Getter
    private final int[] columnIds;

    @Getter
    private int tableId;

    public ColumnCombination(int columnId, int tableId) {
        this(new int[]{columnId}, tableId);
    }

    public ColumnCombination(int[] columnIds, int tableId) {
        this.columnIds = columnIds;
        this.tableId = tableId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnCombination that = (ColumnCombination) o;
        return tableId == that.tableId &&
                Arrays.equals(columnIds, that.columnIds);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(tableId);
        result = 31 * result + Arrays.hashCode(columnIds);
        return result;
    }

    //    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        ColumnCombination that = (ColumnCombination) o;
//        return Arrays.equals(columnIds, that.columnIds);
//    }
//
//    @Override
//    public int hashCode() {
//        return Arrays.hashCode(columnIds);
//    }
}
