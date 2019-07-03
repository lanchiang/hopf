package de.hpi.isg.element;

/**
 * @author Lan Jiang
 */
public class Column {

    private int columnId;

    private String columnName;

    public Column(int columnId, String columnName, int tableId) {
        this.columnId = columnId;
        this.columnName = columnName;
        this.tableId = tableId;
    }

    /**
     * The table id of which this column belong to.
     */
    private int tableId;

    public int getColumnId() {
        return columnId;
    }

    public String getColumnName() {
        return columnName;
    }

    public int getTableId() {
        return tableId;
    }
}
