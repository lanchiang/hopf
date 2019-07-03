package de.hpi.isg.binning;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lan Jiang
 */
public class BinningUtils {

    public static double binningSimilarity(Binning.Bin[] bins1, Binning.Bin[] bins2) {
        final long bin1Sum = Arrays.stream(bins1).mapToLong(Binning.Bin::getCount).sum();
        List<Double> normalizedBin1 = Arrays.stream(bins1).map(bin -> (double) bin.getCount() / (double) bin1Sum).collect(Collectors.toList());

        final long bin2Sum = Arrays.stream(bins2).mapToLong(Binning.Bin::getCount).sum();
        List<Double> normalizedBin2 = Arrays.stream(bins2).map(bin -> (double) bin.getCount() / (double) bin2Sum).collect(Collectors.toList());

        double similarity = 0d;
        for (int i=0;i<bins1.length; i++) {
            similarity += Math.sqrt(normalizedBin1.get(i) * normalizedBin2.get(i));
        }
        return similarity;
    }
}
