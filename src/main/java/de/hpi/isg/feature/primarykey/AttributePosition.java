package de.hpi.isg.feature.primarykey;

import de.hpi.isg.element.Column;
import de.hpi.isg.element.Table;
import de.hpi.isg.element.UniqueColumnCombinationInstance;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lan Jiang
 */
public class AttributePosition extends PrimaryKeyFeature {

    private Map<Integer, List<Integer>> orderedColumnIdByTableId;

    private Map<UniqueColumnCombinationInstance, Table> tableByUCC;

    private List<Column> columns;

    public AttributePosition(List<Column> columns,
                             final Map<Table, List<UniqueColumnCombinationInstance>> uccInstancesByTable) {
        this.columns = columns;
        this.tableByUCC = new HashMap<>();
        uccInstancesByTable.forEach((table, uccInstances) -> uccInstances.forEach(uccInstance -> this.tableByUCC.putIfAbsent(uccInstance, table)));

        this.orderedColumnIdByTableId = columns.stream()
                .collect(Collectors.groupingBy(Column::getTableId, Collectors.mapping(Column::getColumnId, Collectors.toList())));

        this.orderedColumnIdByTableId.forEach((key, value) -> Collections.sort(value));
    }

    @Override
    public void score(Collection<UniqueColumnCombinationInstance> uccInstances) {
        int columnId = columns.stream()
                .filter(column -> column.getColumnName().equals("ProductKey") && column.getTableName().equals("DimProduct"))
                .findFirst().get().getColumnId();
        for (UniqueColumnCombinationInstance uccInstance : uccInstances) {
            if (uccInstance.getUcc().getArity() == 1 && uccInstance.getUcc().getColumnCombination().getColumnIds()[0] == columnId) {
                int stop = 0;
            }
            Table table = tableByUCC.get(uccInstance);
            List<Integer> columnIndex = orderedColumnIdByTableId.get(table.getTableId());
            int[] columnIds = uccInstance.getUcc().getColumnCombination().getColumnIds();
            double left = 1 / left(columnIds, columnIndex);
            double between = 1 / between(columnIds, columnIndex);

            double positionScore = (left + between) / 2;
            uccInstance.getFeatureScores().putIfAbsent(getClass().getSimpleName(), positionScore);
        }

//        normalize(uccInstances);
    }

    private double left(int[] columnIds, List<Integer> columnIndex) {
        double left = 1;
//        OptionalInt optionalMinColumnId = Arrays.stream(columnIds).map(IntUnaryOperator.identity()).min();
//        if (!optionalMinColumnId.isPresent()) {
//            throw new RuntimeException("Cannot find the smallest column id");
//        }
//        left += columnIndex.indexOf(optionalMinColumnId.getAsInt());

        for (int columnId : columnIds) {
            left += columnIndex.indexOf(columnId);
        }
        return left / (double) columnIds.length;
    }

    private double between(int[] columnIds, List<Integer> columnIndex) {
        double between = 1;
//        OptionalInt optionalMinColumnId = Arrays.stream(columnIds).map(IntUnaryOperator.identity()).min();
//        OptionalInt optionalMaxColumnId = Arrays.stream(columnIds).map(IntUnaryOperator.identity()).max();
//        if (!optionalMaxColumnId.isPresent()) {
//            throw new RuntimeException("Cannot find the biggest column id");
//        }
//        if (!optionalMinColumnId.isPresent()) {
//            throw new RuntimeException("Cannot find the smallest column id");
//        }
//        between += columnIndex.indexOf(optionalMaxColumnId.getAsInt() - columnIndex.indexOf(optionalMinColumnId.getAsInt()));

        for (int i = 0; i < columnIds.length - 1; i++) {
            between += (columnIndex.indexOf(columnIds[i + 1]) - columnIndex.indexOf(columnIds[i]));
        }
        return between / (double) columnIds.length;
    }
}
