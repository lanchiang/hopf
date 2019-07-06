package de.hpi.isg.tools;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Lan Jiang
 */
public class ColumnStatisticsJsonReader extends ProfileReader {

    private ObjectMapper mapper = new ObjectMapper();

    private Set<ColumnStatisticsJsonObject> columnStatisticsJsonObjects = new HashSet<>();

    @Override
    public void processLine(String line) {
        ColumnStatisticsJsonObject result = null;
        try {
            result = this.mapper.readValue(line, ColumnStatisticsJsonObject.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        columnStatisticsJsonObjects.add(result);
    }

    public Set<ColumnStatisticsJsonObject> getColumnStatisticsJsonObjects() {
        return columnStatisticsJsonObjects;
    }
}
