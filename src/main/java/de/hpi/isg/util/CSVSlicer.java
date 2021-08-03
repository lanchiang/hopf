package de.hpi.isg.util;

import com.opencsv.*;

import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Slice a csv file according to the delimiters, each slice is an individual column of the csv table.
 *
 * @author Lan Jiang
 */
public class CSVSlicer {

    private final File inputFile;

    private final Parameters parameters;

    public CSVSlicer(File inputFile, Parameters parameters) {
        this.inputFile = inputFile;
        this.parameters = parameters;
    }

    /**
     * Create the slices of the input data file.
     * @return
     */
    public Map<String, List<String>> generateDataSlices() {
        Map<String, List<String>> dataSlices = null;
        try {
            dataSlices = read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataSlices;
    }

    private Map<String, List<String>> read() throws IOException {
        char delimiter;
        if ("\\t".equals(this.parameters.delimiter)) {
            delimiter = '\t';
        } else {
            delimiter = this.parameters.delimiter.charAt(0);
        }
        char quote = this.parameters.quoteCharacter.charAt(0);
        CSVParser csvParser = new CSVParserBuilder()
                .withSeparator(delimiter)
                .withQuoteChar(quote).build();
        CSVReader csvReader = new CSVReaderBuilder(new FileReader(this.inputFile))
                .withCSVParser(csvParser).build();

        String[] line;
        List<List<String>> data = new ArrayList<>();
        String[] headers;
        if (parameters.hasHeader) {
            headers = csvReader.readNext();
            Arrays.stream(headers).forEachOrdered(header -> data.add(new LinkedList<>()));
        } else {
            // Todo
            csvReader.close();
            throw new RuntimeException("Not implemented yet.");
        }
        while ((line = csvReader.readNext()) != null) {
            for (int i = 0; i < line.length; i++) {
                if (!line[i].equals(parameters.nullString)) {
                    data.get(i).add(line[i]);
                }
            }
        }
        csvReader.close();
        String fileName = this.inputFile.getName();
        Map<String, List<String>> dataSliceMap = new HashMap<>();
        for (int i = 0; i < data.size(); i++) {
            dataSliceMap.putIfAbsent(fileName + "." + headers[i] + ".csv", data.get(i));
        }
        return dataSliceMap;
    }
}
