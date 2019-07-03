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

    public AttributePosition(List<Column> columns,
                             final Map<Table, Set<UniqueColumnCombinationInstance>> uccInstancesByTable) {
        this.tableByUCC = new HashMap<>();
        uccInstancesByTable.forEach((table, uccInstances) -> uccInstances.forEach(uccInstance -> this.tableByUCC.putIfAbsent(uccInstance, table)));

        this.orderedColumnIdByTableId = columns.stream()
                .collect(Collectors.groupingBy(Column::getTableId, Collectors.mapping(Column::getColumnId, Collectors.toList())));

        this.orderedColumnIdByTableId.forEach((key, value) -> Collections.sort(value));
    }

    @Override
    public void score(Collection<UniqueColumnCombinationInstance> uccInstances) {
        for (UniqueColumnCombinationInstance uccInstance : uccInstances) {
            Table table = tableByUCC.get(uccInstance);
            List<Integer> columnIndex = orderedColumnIdByTableId.get(table.getTableId());
            int[] columnIds = uccInstance.getUcc().getColumnCombination().getColumnIds();
            double left = 1 / (double) (left(columnIds, columnIndex));
            double between = 1 / (double) (between(columnIds, columnIndex));

            double positionScore = (left + between) / 2;
            uccInstance.getFeatureScores().putIfAbsent(getClass().getSimpleName(), positionScore);
        }

        normalize(uccInstances);
    }

    private int left(int[] columnIds, List<Integer> columnIndex) {
        int left = 1;
        for (int columnId : columnIds) {
            left += columnIndex.indexOf(columnId);
        }
        return left;
    }

    private int between(int[] columnIds, List<Integer> columnIndex) {
        int between = 1;
        for (int i = 0; i < columnIds.length - 1; i++) {
            between += (columnIndex.indexOf(columnIds[i + 1]) - columnIndex.indexOf(columnIds[i]));
        }
        return between;
    }
}
