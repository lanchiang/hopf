package de.hpi.isg.element;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Lan Jiang
 */
@EqualsAndHashCode
public class Table {

    @Getter
    private final int tableId;

    @Getter @EqualsAndHashCode.Exclude
    private final int[] columnIds;

    public Table(int tableId, int... columns) {
        this.tableId = tableId;
        this.columnIds = columns;
    }

//    public Column getColumnById(int id) {
//        Optional<Column> optionalColumn = columns.stream().filter(column -> column.getColumnId()==id).findFirst();
//        return optionalColumn.orElse(null);
//    }

    public Column addColumn(String columnName) {
        return null;
    }


}
