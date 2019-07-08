package de.hpi.isg.element;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Lan Jiang
 */
public class Table {

    private int tableId;

    private List<Column> columns;

    public Table(int tableId, List<Column> columns) {
        this.tableId = tableId;
        this.columns = columns;
    }

    public Column getColumnById(int id) {
        Optional<Column> optionalColumn = columns.stream().filter(column -> column.getColumnId()==id).findFirst();
        return optionalColumn.orElse(null);
    }

    public Collection<Column> getColumns() { return columns; }

    public Column addColumn(String columnName) {
        return null;
    }

    public int getTableId() {
        return tableId;
    }
}
