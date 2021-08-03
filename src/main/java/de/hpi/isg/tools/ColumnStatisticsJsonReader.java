package de.hpi.isg.tools;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Lan Jiang
 */
public class ColumnStatisticsJsonReader extends ProfileReader {

    private ObjectMapper mapper = new ObjectMapper();

    private List<ColumnStatisticsJsonObject> columnStatisticsJsonObjects = new LinkedList<>();

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

    public List<ColumnStatisticsJsonObject> getColumnStatisticsJsonObjects() {
        return columnStatisticsJsonObjects;
    }
}
