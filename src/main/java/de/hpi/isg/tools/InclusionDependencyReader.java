package de.hpi.isg.tools;

import de.hpi.isg.constraints.InclusionDependency;
import de.hpi.isg.constraints.UniqueColumnCombination;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
            tableMapping.put(parts[1],parts[0]);
        } else if (isColumnMapping) {
            String[] parts = line.split(MAPPING_SEPARATOR);
            columnMapping.put(parts[1],parts[0]);
        } else if (isResultMapping) {
            String[] parts = line.split(CC_RESULT_SEPARATOR);
            inds.add(new InclusionDependency(null, null)); // Todo: find a unique column id for these columns.
        } else {
            throw new RuntimeException(String.format("Could not process \"{}\".", line));
        }
    }

    public Set<InclusionDependency> getInds() {
        return inds;
    }
}
