package de.hpi.isg.binning;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lan Jiang
 */
public class EqualDepthBinning extends Binning {

    private final static float eps = 1;

    public EqualDepthBinning(Collection<?> data, int numOfBins) {
        super(data, numOfBins);
    }

    @Override
    protected void createBins() {
        dataType = DataTypeSniffer.sniffDataType(data);

        float dataSize = data.size();
        if (dataType == DataTypeSniffer.DataType.Numeric) {
            List<Double> numerics = data.stream().map(tuple -> Double.parseDouble(tuple.toString())).sorted().collect(Collectors.toList());
            double base = numerics.get(0);
            double width = (numerics.get(numerics.size() - 1) - numerics.get(0)) / (double) numOfBins;
            for (int i = 0; i < numOfBins; i++) {
                double lowerBound = i * width + base;
                double upperBound;
                if (i == numOfBins - 1) {
                    upperBound = (i + 1) * width + base + eps;
                } else {
                    upperBound = (i + 1) * width + base;
                }
                long count = numerics.stream().filter(tuple -> tuple >= lowerBound && tuple < upperBound).count();
                Bin bin = new NumericBin(lowerBound, upperBound);
                bin.setCount(count);
                bins[i] = bin;
            }
            numerics.clear();
        } else {
            List<String> strings = data.stream().map(Object::toString).collect(Collectors.toList());
            List<String> distinctStrings = strings.stream().distinct().sorted().collect(Collectors.toList());
            double width = (double) distinctStrings.size() / (double) numOfBins;
            for (int i = 0; i < numOfBins; i++) {
                int lower = (int) (width * i);
                int higher = (int) (width * (i + 1));

                Bin bin;
                if (i == numOfBins - 1) {
                    bin = new TextBin(distinctStrings.get(lower), distinctStrings.get(higher - 1) + " ");
                } else {
                    bin = new TextBin(distinctStrings.get(lower), distinctStrings.get(higher));
                }
                long count = strings.stream()
                        .filter(tuple -> tuple.compareTo(bin.getLowerBound().toString()) >= 0 && tuple.compareTo(bin.getUpperBound().toString()) < 0)
                        .count();
                bin.setCount(count);
                bins[i] = bin;
            }
            distinctStrings.clear();
            strings.clear();
        }
    }
}
