package de.hpi.isg.tools;

import de.hpi.isg.constraints.ColumnStatistics;
import de.hpi.isg.constraints.InclusionDependency;
import org.apache.commons.lang3.Validate;

import java.util.*;

/**
 * @author Lan Jiang
 */
public class InclusionDependencyReader extends ProfileReader {

    private boolean isTableMapping = false;
    private boolean isColumnMapping = false;
    private boolean isResultMapping = false;

    private Map<String, String> tableMapping = new HashMap<>();
    private Map<String, String> columnMapping = new HashMap<>();

    private Set<InclusionDependency> inds = new HashSet<>();

    private final Map<String, Integer> columnIdByTableColumnName = new HashMap<>();

    public InclusionDependencyReader(Set<ColumnStatistics> columnStatistics) {
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
            String[] parts = line.split(BELONGINGTO_MARKER);
            String[] lhs = parts[0].split(CC_RESULT_SEPARATOR);
            String[] rhs = parts[1].split(CC_RESULT_SEPARATOR);
            Validate.isTrue(lhs.length == rhs.length);

            Map<Integer, Integer> lhsToRhs = new TreeMap<>();

            for (int i = 0; i < lhs.length; i++) {
                String[] tableIdAndColumnNameDependent = columnMapping.get(lhs[i]).split(TABLE_COLUMN_SEPARATOR);
                String tableNameDep = tableMapping.get(tableIdAndColumnNameDependent[0]);
                int dependentId = columnIdByTableColumnName.get(tableNameDep + TABLE_COLUMN_SEPARATOR + tableIdAndColumnNameDependent[1]);

                String[] tableIdAndColumnNameReference = columnMapping.get(rhs[i]).split(TABLE_COLUMN_SEPARATOR);
                String tableNameRef = tableMapping.get(tableIdAndColumnNameReference[0]);
                int referenceId = columnIdByTableColumnName.get(tableNameRef + TABLE_COLUMN_SEPARATOR + tableIdAndColumnNameReference[1]);
                lhsToRhs.putIfAbsent(dependentId, referenceId);
            }

            int[] dependentColumnIds = new int[lhs.length];
            int[] referenceColumnIds = new int[rhs.length];
            int i = 0;
            for (Iterator<Integer> iterator = lhsToRhs.keySet().iterator(); iterator.hasNext(); i++) {
                dependentColumnIds[i] = iterator.next();
                referenceColumnIds[i] = lhsToRhs.get(dependentColumnIds[i]);
            }

            inds.add(new InclusionDependency(dependentColumnIds, referenceColumnIds)); // Todo: find a unique column id for these columns.
        } else {
            throw new RuntimeException("Could not process " + line);
        }
    }

    public Set<InclusionDependency> getInds() {
        return inds;
    }
}
