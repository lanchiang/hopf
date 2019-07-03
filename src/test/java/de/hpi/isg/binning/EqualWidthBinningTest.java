package de.hpi.isg.binning;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Lan Jiang
 */
public class EqualWidthBinningTest {

    @Test
    public void numericsEqualWidthBinningTest() {
        Double[] dataArray = new Double[]{0.5, 0.1, 0.3, 0.6, 1.3, 3.3, 2.3, 6.0, 4.5};
        Collection<?> data = Arrays.asList(dataArray);

        Binning binning = new EqualWidthBinning(data, 3);
        binning.createBins();
        binning.assignData();

        Assert.assertEquals(binning.getNumOfBins(), 3);
        Assert.assertEquals(binning.getDataType(), DataTypeSniffer.DataType.Numeric);
        Assert.assertEquals(binning.getBins()[0].getCount(), 3);
        Assert.assertEquals(binning.getBins()[1].getCount(), 3);
        Assert.assertEquals(binning.getBins()[2].getCount(), 3);
        Assert.assertEquals(binning.getBins()[0].getLowerBound(), 0.1);
        Assert.assertEquals(binning.getBins()[1].getLowerBound(), 0.6);
        Assert.assertEquals(binning.getBins()[2].getLowerBound(), 3.3);
        Assert.assertEquals(binning.getBins()[0].getUpperBound(), 0.6);
        Assert.assertEquals(binning.getBins()[1].getUpperBound(), 3.3);
        Assert.assertEquals(binning.getBins()[2].getUpperBound(), 7.0);
    }

    @Test
    public void textEqualWidthBinningTest() {
        String[] dataArray = new String[]{"abc","0.5","0.3", "name", "0.6", "test", "1.3, 3.3", "binning"};
        Collection<?> data = Arrays.asList(dataArray);
        Binning binning = new EqualWidthBinning(data, 2);
        binning.assignData();

        Assert.assertEquals(binning.getNumOfBins(), 2);
        Assert.assertEquals(binning.getDataType(), DataTypeSniffer.DataType.Text);
        Assert.assertEquals(binning.getBins()[0].getCount(), 4);
        Assert.assertEquals(binning.getBins()[1].getCount(), 4);
        Assert.assertEquals(binning.getBins()[0].getLowerBound(), "0.3");
        Assert.assertEquals(binning.getBins()[1].getLowerBound(), "abc");
        Assert.assertEquals(binning.getBins()[0].getUpperBound(), "abc");
        Assert.assertEquals(binning.getBins()[1].getUpperBound(), "test ");
    }
}
