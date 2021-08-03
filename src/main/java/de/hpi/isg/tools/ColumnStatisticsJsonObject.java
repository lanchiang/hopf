package de.hpi.isg.tools;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.hpi.isg.constraints.ColumnStatistics;
import de.hpi.isg.tools.json.JsonColumnCombination;
import de.hpi.isg.tools.json.JsonStatisticMap;

import java.util.*;

/**
 * @author Lan Jiang
 */
public class ColumnStatisticsJsonObject {

    @JsonProperty("type")
    private String type;

    @JsonProperty("columnCombination")
    private JsonColumnCombination columnCombination;

    @JsonProperty("statisticMap")
    private JsonStatisticMap statisticMap;

    @JsonProperty("data")
    private Collection<String> columnData;

    public ColumnStatistics createColumnStatistics() {
        ColumnStatistics columnStatistics = new ColumnStatistics();
        columnStatistics.setColumnName(columnCombination.getColumnIdentifiers().get(0).getColumnIdentifier());
        columnStatistics.setTableName(columnCombination.getColumnIdentifiers().get(0).getTableIdentifier());

        columnStatistics.setType(statisticMap.getDataType().getValue());
        columnStatistics.setNumDistinctValues(statisticMap.getNumOfDistinct().getValue());
        columnStatistics.setNumNulls(statisticMap.getNumOfNull().getValue());
        columnStatistics.setFillStatus(statisticMap.getNullPercentage().getValue());
        columnStatistics.setUniqueness(statisticMap.getDistinctPercentage().getValue());

        columnStatistics.setEntropy(statisticMap.getEntropy().getValue());

        if (Optional.ofNullable(statisticMap.getLongestString()).isPresent()) {
            columnStatistics.setLongestStringValue(statisticMap.getLongestString().getValue());
        }
        if (Optional.ofNullable(statisticMap.getShortestString()).isPresent()) {
            columnStatistics.setShortestStringValue(statisticMap.getShortestString().getValue());
        }
        if (Optional.ofNullable(statisticMap.getMinString()).isPresent()) {
            columnStatistics.setMinStringValue(statisticMap.getMinString().getValue());
        }
        if (Optional.ofNullable(statisticMap.getMaxString()).isPresent()) {
            columnStatistics.setMaxStringValue(statisticMap.getMaxString().getValue());
        }

        if (Optional.ofNullable(statisticMap.getMin()).isPresent()) {
            columnStatistics.setMinValue(statisticMap.getMin().getValue());
        }
        if (Optional.ofNullable(statisticMap.getMax()).isPresent()) {
            columnStatistics.setMaxValue(statisticMap.getMax().getValue());
        }
        if (Optional.ofNullable(statisticMap.getAvg()).isPresent()) {
            columnStatistics.setAverage(statisticMap.getAvg().getValue());
        }
        if (Optional.ofNullable(statisticMap.getStandardDeviation()).isPresent()) {
            columnStatistics.setStandardDeviation(statisticMap.getStandardDeviation().getValue());
        }

        List<Integer> frequencies = new ArrayList<>(statisticMap.getFrequenciesOfItems().getValue());
        List<String> items = new ArrayList<>(statisticMap.getMostFrequentItems().getValue());

        List<ColumnStatistics.ValueOccurrence> valueOccurrences = new LinkedList<>();
        for (int i = 0; i < frequencies.size(); i++) {
            valueOccurrences.add(new ColumnStatistics.ValueOccurrence(items.get(i), frequencies.get(i)));
        }

        columnStatistics.setTopKFrequentValues(valueOccurrences);

        columnStatistics.setColumnValues(columnData);

        return columnStatistics;
    }
}