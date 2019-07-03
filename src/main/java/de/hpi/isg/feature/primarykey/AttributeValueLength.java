package de.hpi.isg.feature.primarykey;

import de.hpi.isg.constraints.*;
import de.hpi.isg.target.UniqueColumnCombinationInstance;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Lan Jiang
 */
public class AttributeValueLength extends PrimaryKeyFeature {

    private final Map<Integer, String> columnTypes;

    private final Map<Integer, Integer> columnMaxLength;

    public AttributeValueLength(Set<ColumnStatistics> columnStatisticss) {
        this.columnTypes = new HashMap<>();

        columnStatisticss.forEach(columnStatistics -> {
            int columnId = columnStatistics.getColumnId();
            String columnType = columnStatistics.getType();
            columnTypes.putIfAbsent(columnId, columnType);
        });

        columnMaxLength = new HashMap<>();
        columnStatisticss.forEach(columnStatistics -> {
            int columnId = columnStatistics.getColumnId();
            switch (columnTypes.get(columnId)) {
                case "DATE":
                    columnMaxLength.putIfAbsent(columnId, 10);
                    break;
                case "TIMESTAMP":
                    columnMaxLength.putIfAbsent(columnId, 20);
                    break;
                case "TEXT":
                    columnMaxLength.putIfAbsent(columnId, 50);
                    break;
                case "BOOLEAN":
                    columnMaxLength.putIfAbsent(columnId, 1);
                    break;
                case "UUID":
                    columnMaxLength.putIfAbsent(columnId, 36);
                    break;
                case "NA":
                    columnMaxLength.putIfAbsent(columnId, 0);
                    break;
                default:
                    break;
            }
        });

        columnStatisticss.forEach(columnStatistics -> {
            int columnId = columnStatistics.getColumnId();
            double maxValue = columnStatistics.getMaxValue();
            String longestStringValue = columnStatistics.getLongestStringValue();
            String valueString;
            if (!Double.isNaN(maxValue)) {
                valueString = new BigDecimal(maxValue).toPlainString();
                columnMaxLength.putIfAbsent(columnId, valueString.length());
            } else if (longestStringValue != null) {
                columnMaxLength.putIfAbsent(columnId, longestStringValue.length());
            } else {
                throw new RuntimeException("Cannot get the longest value length of column: " + columnId);
            }
        });
    }

    @Override
    public void score(Collection<UniqueColumnCombinationInstance> uccInstances) {
        for (UniqueColumnCombinationInstance uccInstance : uccInstances) {
            double concatencateScore = 0;
            for (int columnId : uccInstance.getUcc().getColumnCombination().getColumnIds()) {
                double length = columnMaxLength.get(columnId);
                concatencateScore += (1 > (length - 7)) ? 1 : (1 / (length - 7));
            }
            concatencateScore /= (double) uccInstance.getUcc().getArity();
            uccInstance.getFeatureScores().putIfAbsent(getClass().getSimpleName(), concatencateScore);
        }

        normalize(uccInstances);
    }
}
