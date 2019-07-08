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

    public UniqueColumnCombinationReader(Set<ColumnStatistics> columnStatistics) {
        columnStatistics.forEach(columnStat -> columnIdByTableColumnName.putIfAbsent(columnStat.getTableName() + TABLE_COLUMN_SEPARATOR + columnStat.getColumnName(), columnStat.getColumnId()));
    }

    @Override
    public void processLine(String line) {
        if (line.startsWith(TABLE_MARKER)) {
            isTableMapping = true;
            isColumnMapping = false;
            isResultMapping = false;
            return;
        } else if (line.startsWith(COLUMN_MARKER)) {
            isTableMapping = false;
            isColumnMapping = true;
            isResultMapping = false;
            return;
        } else if (line.startsWith(RESULT_MARKER)) {
            isTableMapping = false;
            isColumnMapping = false;
            isResultMapping = true;
            return;
        }

        if (isTableMapping) {
            String[] parts = line.split(MAPPING_SEPARATOR);
            tableMapping.put(parts[1], parts[0]);
        } else if (isColumnMapping) {
            String[] parts = line.split(MAPPING_SEPARATOR);
            columnMapping.put(parts[1], parts[0]);
        } else if (isResultMapping) {
            String[] parts = line.split(CC_RESULT_SEPARATOR);
            List<Integer> columnIdSet = Arrays.stream(parts).map(s -> {
                String[] tableAndColumn = columnMapping.get(s).split(TABLE_COLUMN_SEPARATOR);
                String tableName = tableMapping.get(tableAndColumn[0]);
                return columnIdByTableColumnName.get(tableName + TABLE_COLUMN_SEPARATOR + tableAndColumn[1]);
            }).sorted(Integer::compareTo).collect(Collectors.toList());
            int[] unboxed = new int[columnIdSet.size()];
            for (int i = 0; i < columnIdSet.size(); i++) {
                unboxed[i] = columnIdSet.get(i);
            }

            uccs.add(new UniqueColumnCombination(unboxed));
        } else {
            throw new RuntimeException("Could not process " + line);
        }
    }

    public Set<UniqueColumnCombination> getUccs() {
        return uccs;
    }
}
