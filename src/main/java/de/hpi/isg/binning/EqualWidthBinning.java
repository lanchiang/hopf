package de.hpi.isg.binning;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author Lan Jiang
 */
public class EqualWidthBinning extends Binning {

    public EqualWidthBinning(Collection<?> data, int numOfBins) {
        super(data, numOfBins);
    }

    @Override
    protected void createBins() {
        Set<Object> distinctData = new TreeSet<>(data);

        dataType = DataTypeSniffer.sniffDataType(distinctData);
        if (dataType == DataTypeSniffer.DataType.Numeric) {
            double width = (double) distinctData.size() / (double) numOfBins;
            List<Double> numerics = distinctData.parallelStream()
                    .map(element -> Double.parseDouble(element.toString()))
                    .sorted()
                    .collect(Collectors.toList());
            for (int i = 0; i < numOfBins; i++) {
                int lowerBound = (int) (i * width);
                int higherBound = (int) ((i + 1) * width);
                if (i == numOfBins - 1) {
                    bins[i] = new NumericBin(numerics.get(lowerBound), numerics.get(higherBound - 1) + 1);
                } else {
                    bins[i] = new NumericBin(numerics.get(lowerBound), numerics.get(higherBound));
                }
            }
        } else if (dataType == DataTypeSniffer.DataType.Text) {
            double width = (double) distinctData.size() / (double) numOfBins;
            List<String> strings = distinctData.parallelStream()
                    .map(Object::toString)
                    .sorted()
                    .collect(Collectors.toList());
            for (int i = 0; i < numOfBins; i++) {
                int lower = (int) (width * i);
                int higher = (int) (width * (i + 1));
                if (i == numOfBins - 1) {
                    bins[i] = new TextBin(strings.get(lower), strings.get(higher - 1) + " ");
                } else {
                    bins[i] = new TextBin(strings.get(lower), strings.get(higher));
                }
            }
        } else {
            throw new IllegalArgumentException("Data type is incorrect.");
        }
    }
}
