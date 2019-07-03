package de.hpi.isg.tools;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Lan Jiang
 */
public class ColumnStatisticsObjectMapper {

    @JsonProperty("")
    private String tableName;

    private String columnName;

    private int numOfDistinct;

    private String dataType;

    private double minValue;

    private double maxValue;

    private double average;

    private double nullPercentage;

    private double distinctPercentage;

    private double standardDeviation;

    private double numOfNull;


}
