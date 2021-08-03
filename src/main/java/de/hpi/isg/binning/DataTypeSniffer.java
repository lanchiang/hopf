package de.hpi.isg.binning;

import java.util.Collection;
import java.util.Optional;

/**
 * @author Lan Jiang
 */
public class DataTypeSniffer {

    public enum DataType {
        Numeric, Text
    }

    /**
     * Return the most possible data type of this data collection.
     *
     * @param data
     * @return the sniffed data type of this data collection.
     */
    public static DataType sniffDataType(Collection<?> data) {
        Optional<?> findNonNumeric = data.parallelStream().filter(element -> {
            try {
                Double.parseDouble(element.toString());
            } catch (NumberFormatException e) {
                return true;
            }
            return false;
        }).findFirst();
        return findNonNumeric.isPresent() ? DataType.Text : DataType.Numeric;
    }
}
