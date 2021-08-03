package de.hpi.isg.data;

import de.hpi.isg.DataProfiles;
import de.hpi.isg.constraints.ColumnStatistics;
import de.hpi.isg.constraints.InclusionDependency;
import de.hpi.isg.constraints.UniqueColumnCombination;
import de.hpi.isg.tools.ColumnStatisticsJsonObject;
import de.hpi.isg.tools.ColumnStatisticsJsonReader;
import de.hpi.isg.tools.InclusionDependencyReader;
import de.hpi.isg.tools.UniqueColumnCombinationReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lan
 * @since 2021/7/19
 */
public class DataProfileReader {

    private final String uccPath;
    private final String pkPath;
    private final String indPath;
    private final String fkPath;
    private final String scdpPath;

    private Set<Integer> idPool = new HashSet<>();

    private final DataProfiles dataProfiles;

    public DataProfileReader(final String inputPath) {
        uccPath = Paths.get(inputPath, "UCC").toString();
        pkPath = Paths.get(inputPath, "PK").toString();
        indPath = Paths.get(inputPath, "IND.txt").toString();
        fkPath = Paths.get(inputPath, "FK.txt").toString();
        scdpPath = Paths.get(inputPath, "Basic").toString();
        dataProfiles = new DataProfiles(null);
    }

    /**
     * Read all data profiles into memory
     */
    public DataProfiles loadDataProfiles() {
        // load single column data profiles
        this.dataProfiles.setColumnStatistics(this.loadSCDP());

        // load uccs
        this.dataProfiles.setUccs(this.loadUccs(uccPath));

        // load primary keys
        this.dataProfiles.setPrimaryKeys(this.loadUccs(pkPath));

        // load inds
        this.dataProfiles.setInds(this.loadInds(indPath));

        // load foreign keys
        this.dataProfiles.setInds(this.loadInds(fkPath));

        return this.dataProfiles;
    }

    private Set<ColumnStatistics> loadSCDP() {
        List<ColumnStatistics> scdps = new LinkedList<>();

        File[] scdpFiles = new File(this.scdpPath).listFiles();

        if (scdpFiles == null) {
            throw new RuntimeException("No single column data profile file exists.");
        }

        for (File scdpFile : scdpFiles) {
            if (scdpFile.getName().equals(".DS_Store")) {
                continue;
            }
            ColumnStatisticsJsonReader scdpReader = new ColumnStatisticsJsonReader();
            try {
                Files.lines(scdpFile.toPath()).forEachOrdered(scdpReader::processLine);
            } catch (IOException e) {
                throw new RuntimeException("Could not parse " + scdpFile, e);
            }
            List<ColumnStatistics> partialSCDPs = scdpReader.getColumnStatisticsJsonObjects().stream()
                    .map(ColumnStatisticsJsonObject::createColumnStatistics).collect(Collectors.toList());
            scdps.addAll(partialSCDPs);
        }

        // generate column id and table id if needed.
        Map<String, List<ColumnStatistics>> columnStatisticsByTableName = scdps.stream().collect(Collectors.groupingBy(ColumnStatistics::getTableName, Collectors.toList()));
        columnStatisticsByTableName.forEach((s, columnStats) -> {
            int tableId;
            do {
                tableId = (s.hashCode() + new Random(System.currentTimeMillis()).nextInt()) & 0xfffffff;
            } while (idPool.contains(tableId));
            idPool.add(tableId);

            int finalTableId = tableId;
            columnStats.forEach(columnStat -> {
                columnStat.setTableId(finalTableId);
                int columnId;
                int offset = 1;
                do {
                    columnId = finalTableId + offset;
                    offset++;
//                    columnId = (columnStat.getColumnName().hashCode() + new Random(System.currentTimeMillis()).nextInt()) & 0xfffffff;
                } while (idPool.contains(columnId));
                idPool.add(columnId);
                columnStat.setColumnId(columnId);
            });
        });

        return new HashSet<>(scdps);
    }

    private Set<UniqueColumnCombination> loadUccs(String path) {
        Set<UniqueColumnCombination> uccs = new HashSet<>();
        File[] uccFiles = new File(path).listFiles();
        if (uccFiles == null) {
            throw new RuntimeException("No ucc file exists.");
        }
        for (File uccFile : uccFiles) {
            UniqueColumnCombinationReader uccReader = new UniqueColumnCombinationReader(this.dataProfiles.getColumnStatistics());
            try {
                Files.lines(uccFile.toPath()).forEach(uccReader::processLine);
            } catch (IOException e) {
                throw new RuntimeException("Could not parse " + uccFile, e);
            }
            uccs.addAll(uccReader.getUccs());
        }
        return uccs;
    }

    private Set<InclusionDependency> loadInds(String path) {
        File indFile = new File(path);

        InclusionDependencyReader indReader = new InclusionDependencyReader(this.dataProfiles.getColumnStatistics());
        try {
            Files.lines(indFile.toPath()).forEach(indReader::processLine);
        } catch (IOException e) {
            throw new RuntimeException("Could not parse " + indFile, e);
        }
        return new HashSet<>(indReader.getInds());
    }
}
