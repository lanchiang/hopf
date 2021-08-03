package de.hpi.isg;

import de.hpi.isg.constraints.ColumnStatistics;
import de.hpi.isg.constraints.InclusionDependency;
import de.hpi.isg.constraints.UniqueColumnCombination;
import de.hpi.isg.tools.ColumnStatisticsJsonObject;
import de.hpi.isg.tools.ColumnStatisticsJsonReader;
import de.hpi.isg.tools.InclusionDependencyReader;
import de.hpi.isg.tools.UniqueColumnCombinationReader;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lan Jiang
 */
public class DataProfiles {

    @Getter @Setter
    private Set<UniqueColumnCombination> uccs;

    @Getter @Setter
    private Set<InclusionDependency> inds;

    @Getter @Setter
    private Set<UniqueColumnCombination> primaryKeys;

    @Getter @Setter
    private Set<InclusionDependency> foreignKeys;

    @Getter @Setter
    private Set<ColumnStatistics> columnStatistics;

    @Getter
    private Set<Integer> tableIds = new HashSet<>();

    public static final int BIN_NUMBER = 20;

    public static final String DATA_PATH = "";

    private final String uccPath;
    private final String pkPath;
    private final String indPath;
    private final String fkPath;
    private final String scdpPath;

    public DataProfiles(final String profileBasicPath) {
        uccPath = profileBasicPath + File.separator + "UCC";
        pkPath = profileBasicPath + File.separator + "PK";
        indPath = profileBasicPath + File.separator + "IND.txt";
        fkPath = profileBasicPath + File.separator + "FK.txt";
        scdpPath = profileBasicPath + File.separator + "Basic";
    }

    /**
     * Read various data profiles from files.
     */
    public void readConstraints() {
        // load single column data profiles
        columnStatistics = loadSCDP(scdpPath);

        // read uccs
        uccs = loadUccs(uccPath);

        // read primary keys
        primaryKeys = loadUccs(pkPath);

        // read inds
        inds = loadInds(indPath);

        // read foreign keys
        foreignKeys = loadInds(fkPath);
    }

    public Set<UniqueColumnCombination> getUccs() {
        return uccs;
    }

    public Set<InclusionDependency> getInds() {
        return inds;
    }

    public Set<UniqueColumnCombination> getPrimaryKeys() {
        return primaryKeys;
    }

    public Set<InclusionDependency> getForeignKeys() {
        return foreignKeys;
    }

    public Set<ColumnStatistics> getColumnStatistics() {
        return columnStatistics;
    }

    public ColumnStatistics getStatByTableAndColumnName(String tableName, String columnName) {
        Optional<ColumnStatistics> optionalColumnStatistics =
                columnStatistics.stream().filter(colStat -> colStat.getTableName().equals(tableName) && colStat.getColumnName().equals(columnName)).findFirst();
        return optionalColumnStatistics.orElse(null);
    }

    private Set<UniqueColumnCombination> loadUccs(String path) {
        Set<UniqueColumnCombination> uccs = new HashSet<>();
        File[] uccFiles = new File(path).listFiles();
        if (uccFiles == null) {
            throw new RuntimeException("No ucc file exists.");
        }
        for (File uccFile : uccFiles) {
            UniqueColumnCombinationReader uccReader = new UniqueColumnCombinationReader(this.columnStatistics);
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

        InclusionDependencyReader indReader = new InclusionDependencyReader(this.columnStatistics);
        try {
            Files.lines(indFile.toPath()).forEach(indReader::processLine);
        } catch (IOException e) {
            throw new RuntimeException("Could not parse " + indFile, e);
        }
        return new HashSet<>(indReader.getInds());
    }

    private Set<ColumnStatistics> loadSCDP(String path) {
        List<ColumnStatistics> scdps = new LinkedList<>();

        File[] scdpFiles = new File(path).listFiles();
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
            } while (tableIds.contains(tableId));
            tableIds.add(tableId);

            int finalTableId = tableId;
            columnStats.forEach(columnStat -> {
                columnStat.setTableId(finalTableId);
                int columnId;
                int offset = 1;
                do {
                    columnId = finalTableId + offset;
                    offset++;
//                    columnId = (columnStat.getColumnName().hashCode() + new Random(System.currentTimeMillis()).nextInt()) & 0xfffffff;
                } while (tableIds.contains(columnId));
                tableIds.add(columnId);
                columnStat.setColumnId(columnId);
            });
        });

        return new HashSet<>(scdps);
    }
}
