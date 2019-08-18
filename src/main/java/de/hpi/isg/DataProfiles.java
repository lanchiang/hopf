package de.hpi.isg;

import de.hpi.isg.constraints.ColumnStatistics;
import de.hpi.isg.constraints.InclusionDependency;
import de.hpi.isg.constraints.UniqueColumnCombination;
import de.hpi.isg.tools.ColumnStatisticsJsonObject;
import de.hpi.isg.tools.ColumnStatisticsJsonReader;
import de.hpi.isg.tools.InclusionDependencyReader;
import de.hpi.isg.tools.UniqueColumnCombinationReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Lan Jiang
 */
public class DataProfiles {

    private Set<UniqueColumnCombination> uccs;

    private Set<InclusionDependency> inds;

    private Set<UniqueColumnCombination> primaryKeys;

    private Set<InclusionDependency> foreignKeys;

    private Set<ColumnStatistics> columnStatistics;

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
        Set<ColumnStatistics> scdps = new HashSet<>();

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
                Files.lines(scdpFile.toPath()).forEach(scdpReader::processLine);
            } catch (IOException e) {
                throw new RuntimeException("Could not parse " + scdpFile, e);
            }
            Set<ColumnStatistics> partialSCDPs = scdpReader.getColumnStatisticsJsonObjects().stream()
                    .map(ColumnStatisticsJsonObject::createColumnStatistics).collect(Collectors.toSet());
            scdps.addAll(partialSCDPs);
        }

        // generate column id and table id if needed.
        Map<String, Set<ColumnStatistics>> columnStatisticsByTableName = scdps.stream().collect(Collectors.groupingBy(ColumnStatistics::getTableName, Collectors.toSet()));
        columnStatisticsByTableName.forEach((s, columnStats) -> {
            int tableId = (s.hashCode() & 0xfffffff);
            columnStats.forEach(columnStat -> {
                columnStat.setTableId(tableId);
                columnStat.setColumnId((columnStat.getColumnName().hashCode()) & 0xfffffff);
            });
        });

        return scdps;
    }
}
