package de.hpi.isg.binning;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lan Jiang
 */
abstract public class Binning {

    protected final int numOfBins;

    protected final Bin[] bins;

    protected final Collection<?> data;

    protected DataTypeSniffer.DataType dataType;

    Binning(Collection<?> data, int numOfBins) {
        this.data = data;
        this.numOfBins = numOfBins;
        bins = new Bin[this.numOfBins];
    }

    /**
     * Create <code>numOfBins</code> bins from the given <code>data</code>. The bins are left inclusive and right exclusive.
     */
    abstract protected void createBins();

    /**
     * Assign each of the data points in the data collection used to create the bins into the corresponding bin.
     */
    public void assignOwnData() {
        if (Arrays.stream(bins).anyMatch(Objects::isNull)) {
            createBins();
        }

        assignData(data, bins);
    }

    public Bin[] assignData(Collection<?> data) {
        if (Arrays.stream(bins).anyMatch(Objects::isNull)) {
            throw new RuntimeException("The base binning is not done.");
        }

        DataTypeSniffer.DataType guestDataType = DataTypeSniffer.sniffDataType(data);

        if (guestDataType != this.dataType) {
            throw new IllegalArgumentException("Data type does not conform");
        }

        Bin[] guestBins = new Bin[this.bins.length];
        for (int i=0; i< bins.length; i++) {
            guestBins[i] = new Bin(bins[i].getLowerBound(), bins[i].getUpperBound());
        }

        assignData(data, guestBins);
        return guestBins;
    }

    private void assignData(Collection<?> data, Bin[] bins) {
        if (dataType == DataTypeSniffer.DataType.Numeric) {
            int binCursor = 0;

            Queue<Double> dataQueue = data.stream()
                    .map(element -> Double.parseDouble(element.toString()))
                    .sorted().collect(Collectors.toCollection(LinkedList::new));

            Collection<Object> dataPoints = new LinkedList<>();
            while (!dataQueue.isEmpty()) {
                double dataPoint = dataQueue.peek();
                if (dataPoint < Double.parseDouble(bins[binCursor].getUpperBound().toString())) {
                    dataPoints.add(dataQueue.poll());
                } else {
                    bins[binCursor].fillBin(dataPoints);
                    dataPoints = new LinkedList<>();
                    binCursor++;
                    if (binCursor == bins.length) {
                        throw new RuntimeException("Guest data out of range");
                    }
                }
            }
            bins[binCursor].fillBin(dataPoints);
        } else if (dataType == DataTypeSniffer.DataType.Text) {
            int binCursor = 0;

            Queue<String> orderedDataQueue = data.stream().map(Object::toString).sorted().collect(Collectors.toCollection(LinkedList::new));

            Collection<Object> dataPoints = new LinkedList<>();
            while (!orderedDataQueue.isEmpty()) {
                String dataPoint = orderedDataQueue.peek();
                if (dataPoint.compareTo(bins[binCursor].getUpperBound().toString()) < 0) {
                    dataPoints.add(orderedDataQueue.poll());
                } else {
                    bins[binCursor].fillBin(dataPoints);
                    dataPoints = new LinkedList<>();
                    binCursor++;
                    if (binCursor == bins.length) {
                        throw new RuntimeException("Guest data out of range");
                    }
                }
            }
            bins[binCursor].fillBin(dataPoints);
        } else {
            throw new IllegalArgumentException("Data type does not exist.");
        }
    }

    /**
     * Get the data points in a bin specified by its index.
     *
     * @param index
     * @return
     */
    public Collection<?> getDataByBinIndex(int index) {
        return this.bins[index].getData();
    }

    public int getNumOfBins() {
        return numOfBins;
    }

    public Bin[] getBins() {
        return bins;
    }

    public DataTypeSniffer.DataType getDataType() {
        return dataType;
    }

    public class Bin {

        private long count = 0;

        private Object lowerBound;

        private Object upperBound;

        private Collection<Object> data;

        public Bin(Object lowerBound, Object upperBound) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        public void fillBin(Collection<Object> data) {
            this.data = data;
            this.count = data.size();
        }

        public Collection<Object> getData() {
            return data;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public Object getLowerBound() {
            return lowerBound;
        }

        public Object getUpperBound() {
            return upperBound;
        }
    }

    public class NumericBin extends Bin {

        NumericBin(double lowerBound, double upperBound) {
            super(lowerBound, upperBound);
        }
    }

    public class TextBin extends Bin {

        TextBin(String lowerBound, String upperBound) {
            super(lowerBound, upperBound);
        }
    }
}
