package de.hpi.isg.tools;

import de.hpi.isg.constraints.ColumnStatistics;
import de.hpi.isg.constraints.UniqueColumnCombination;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lan Jiang
 */
public class UniqueColumnCombinationReader extends ProfileReader {

    private boolean isTableMapping = false;
    private boolean isColumnMapping = false;
    private boolean isResultMapping = false;

    private Map<String, String> tableMapping = new HashMap<>();
    private Map<String, String> columnMapping = new HashMap<>();

    private Set<UniqueColumnCombination> uccs = new HashSet<>();

    private final Map<String, Integer> columnIdByTableColumnName = new HashMap<>();

    private final Set<ColumnStatistics> columnStatistics;

    private Map<String, Integer> columnIdByColumnName;

    private int tableId;

    public UniqueColumnCombinationReader(Set<ColumnStatistics> columnStatistics) {
        this.columnStatistics = columnStatistics;
        columnStatistics.forEach(columnStat -> columnIdByTableColumnName.putIfAbsent(columnStat.getTableName() + TABLE_COLUMN_SEPARATOR + columnStat.getColumnName(), columnStat.getColumnId()));
    }

    @Override
    public void processLine(String line) {
        if (TABLE_MARKER.startsWith(line)) {
            isTableMapping = true;
            isColumnMapping = false;
            isResultMapping = false;
            return;
        } else if (COLUMN_MARKER.startsWith(line)) {
            isTableMapping = false;
            isColumnMapping = true;
            isResultMapping = false;
            return;
        } else if (RESULT_MARKER.startsWith(line)) {
            isTableMapping = false;
            isColumnMapping = false;
            isResultMapping = true;
            return;
        }

        if (isTableMapping) {
            String[] parts = line.split(MAPPING_SEPARATOR);
            tableMapping.put(parts[1], parts[0]);
            String tableName = parts[0];
            Optional<ColumnStatistics> colStatByTableName = columnStatistics.stream().filter(colStat -> colStat.getTableName().equals(tableName)).findFirst();

            if (colStatByTableName.isPresent()) {
                this.tableId = colStatByTableName.get().getTableId();
            } else {
                throw new RuntimeException("Cannot find the table id.");
            }

            this.columnIdByColumnName = this.columnStatistics.stream()
                    .filter(colStat -> colStat.getTableName().equals(tableName))
                    .collect(Collectors.toMap(ColumnStatistics::getColumnName, ColumnStatistics::getColumnId));
        } else if (isColumnMapping) {
            String[] parts = line.split(MAPPING_SEPARATOR);
            columnMapping.put(parts[1], parts[0]);
        } else if (isResultMapping) {
            String[] parts = line.split(CC_RESULT_SEPARATOR);

            List<Integer> columnIds = Arrays.stream(parts)
                    .map(str -> {
                        String[] tableAndColumn = columnMapping.get(str).split(TABLE_COLUMN_SEPARATOR);
                        return this.columnIdByColumnName.get(tableAndColumn[1]);
                    }).sorted(Integer::compareTo).collect(Collectors.toList());

            uccs.add(new UniqueColumnCombination(tableId, columnIds.stream().mapToInt(Integer::intValue).toArray()));
        } else {
            throw new RuntimeException("Could not process " + line);
        }
    }

    public Set<UniqueColumnCombination> getUccs() {
        return uccs;
    }
}
