package de.hpi.isg.element;

import lombok.Getter;

/**
 * @author Lan Jiang
 */
public class Column {

    @Getter
    private final int columnId;

    @Getter
    private final String columnName;

    /**
     * The table id of which this column belong to.
     */
    @Getter
    private final int tableId;

    @Getter
    private final String tableName;

    public Column(int columnId, String columnName, int tableId, String tableName) {
        this.columnId = columnId;
        this.columnName = columnName;
        this.tableId = tableId;
        this.tableName = tableName;
    }
}
