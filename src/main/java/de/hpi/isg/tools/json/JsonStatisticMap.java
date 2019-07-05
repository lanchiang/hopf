package de.hpi.isg.tools.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Collection;

/**
 * @author Lan Jiang
 */
public class JsonStatisticMap {

    @JsonProperty("Number of Distinct Values")
    @Getter
    private StatisticValue<Integer> numOfDistinct;

    @JsonProperty("Data Type")
    @Getter private StatisticValue<String> dataType;

    @JsonProperty("Min")
    @Getter private StatisticValue<Double> min;

    @JsonProperty("Avg.")
    @Getter private StatisticValue<Double> avg;

    @JsonProperty("Percentage of Nulls")
    @Getter private StatisticValue<Double> nullPercentage;

    @JsonProperty("Percentage of Distinct Values")
    @Getter private StatisticValue<Double> distinctPercentage;

    @JsonProperty("Max")
    @Getter private StatisticValue<Double> max;

    @JsonProperty("Standard Deviation")
    @Getter private StatisticValue<Double> standardDeviation;

    @JsonProperty("Nulls")
    @Getter private StatisticValue<Long> numOfNull;

    @JsonProperty("Min String")
    @Getter private StatisticValue<String> minString;

    @JsonProperty("Max String")
    @Getter private StatisticValue<String> maxString;

    @JsonProperty("Shortest String")
    @Getter private StatisticValue<String> shortestString;

    @JsonProperty("Longest String")
    @Getter private StatisticValue<String> longestString;

    @JsonProperty("Frequency Of Top 10 Frequent Items")
    @Getter private TopItems<Integer> frequenciesOfItems;

    @JsonProperty("Top 10 frequent items")
    @Getter private TopItems<String> mostFrequentItems;

    public class StatisticValue<T> {

        @JsonProperty("type")
        @Getter private String statisticType;

        @JsonProperty("value")
        @Getter private T value;
    }

    public class TopItems<T> {

        @JsonProperty("type")
        @Getter private String type;

        @JsonProperty("value")
        @Getter private Collection<T> value;
    }
}