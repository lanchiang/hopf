package de.hpi.isg.constraints;

import java.util.List;

/**
 * @author Lan Jiang
 */
public class ColumnStatistics implements Constraint {

    private long numNulls = -1, numDistinctValues = -1;

    private double fillStatus = Double.NaN, uniqueness = Double.NaN;

    public List<ValueOccurrence> topKFrequentValues;

    private String minStringValue, maxStringValue, shortestStringValue, longestStringValue;

    private String type;

    private double minValue = Double.NaN, maxValue = Double.NaN;

    private double standardDeviation = Double.NaN;

    private double average = Double.NaN;

    private int columnId;

    private String columnName;

    private int tableId;

    private String tableName;

    public long getNumNulls() {
        return numNulls;
    }

    public void setNumNulls(long numNulls) {
        this.numNulls = numNulls;
    }

    public long getNumDistinctValues() {
        return numDistinctValues;
    }

    public void setNumDistinctValues(long numDistinctValues) {
        this.numDistinctValues = numDistinctValues;
    }

    public double getFillStatus() {
        return fillStatus;
    }

    public void setFillStatus(double fillStatus) {
        this.fillStatus = fillStatus;
    }

    public double getUniqueness() {
        return uniqueness;
    }

    public void setUniqueness(double uniqueness) {
        this.uniqueness = uniqueness;
    }

    public List<ColumnStatistics.ValueOccurrence> getTopKFrequentValues() {
        return topKFrequentValues;
    }

    public void setTopKFrequentValues(List<ColumnStatistics.ValueOccurrence> topKFrequentValues) {
        this.topKFrequentValues = topKFrequentValues;
    }

    public String getMinStringValue() {
        return minStringValue;
    }

    public void setMinStringValue(String minStringValue) {
        this.minStringValue = minStringValue;
    }

    public String getMaxStringValue() {
        return maxStringValue;
    }

    public void setMaxStringValue(String maxStringValue) {
        this.maxStringValue = maxStringValue;
    }

    public String getShortestStringValue() {
        return shortestStringValue;
    }

    public void setShortestStringValue(String shortestStringValue) {
        this.shortestStringValue = shortestStringValue;
    }

    public String getLongestStringValue() {
        return longestStringValue;
    }

    public void setLongestStringValue(String longestStringValue) {
        this.longestStringValue = longestStringValue;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public int getColumnId() {
        return this.columnId;
    }

    public void setColumnId(int columnId) {
        this.columnId = columnId;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * This class describes a value and the number of its occurrences (in a column). Instances are primarily ordered by
     * their count and by their value as tie breaker.
     */
    public static class ValueOccurrence implements Comparable<ColumnStatistics.ValueOccurrence> {

        private final String value;

        private final long numOccurrences;

        public ValueOccurrence(String value, long numOccurrences) {
            this.value = value;
            this.numOccurrences = numOccurrences;
        }

        public String getValue() {
            return value;
        }

        public long getNumOccurrences() {
            return numOccurrences;
        }

        @Override
        public int compareTo(ColumnStatistics.ValueOccurrence that) {
            int result = Long.compare(this.getNumOccurrences(), that.getNumOccurrences());
            return result == 0 ? this.getValue().compareTo(that.getValue()) : result;
        }
    }
}
