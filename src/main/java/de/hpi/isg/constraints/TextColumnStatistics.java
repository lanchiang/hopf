package de.hpi.isg.constraints;

/**
 * @author Lan Jiang
 */
public class TextColumnStatistics implements Constraint {

    /**
     * Special values in a column.
     */
    private String minValue, maxValue, shortestValue, longestValue;

    /**
     * The type of strings contained in a column, such as JSON or UUID.
     */
    private String subtype;

    private final int columnId;

    public TextColumnStatistics(int columnId) {
        this.columnId = columnId;
    }

    public int getColumnId() {
        return this.columnId;
    }

    public String getMinValue() {
        return minValue;
    }

    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    public String getShortestValue() {
        return shortestValue;
    }

    public void setShortestValue(String shortestValue) {
        this.shortestValue = shortestValue;
    }

    public String getLongestValue() {
        return longestValue;
    }

    public void setLongestValue(String longestValue) {
        this.longestValue = longestValue;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }
}
