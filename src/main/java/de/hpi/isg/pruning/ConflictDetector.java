package de.hpi.isg.pruning;

import de.hpi.isg.constraints.ColumnStatistics;
import de.hpi.isg.constraints.InclusionDependency;
import de.hpi.isg.element.InclusionDependencyInstance;
import de.hpi.isg.element.Table;
import de.hpi.isg.element.UniqueColumnCombinationInstance;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The conflict rules are used to prune inclusion dependencies that are impossible to be foreign keys.
 * Three pruning rules are proposed: 1) uniqueness of foreign keys; 2) non-cyclic reference; 3) schema connectivity.
 *
 *
 * @author Lan Jiang
 */
public class ConflictDetector {

    private final int[][] columnReferenceMatrix;
    private final int dimension;
    private final Map<Integer, Integer> columnIdIndex;

    private final TableReferenceGraph tableReferenceGraph;

    public ConflictDetector(Set<ColumnStatistics> columnStatistics) {
        Set<Integer> columnIds = columnStatistics.stream().map(ColumnStatistics::getColumnId).collect(Collectors.toSet());
        dimension = columnIds.size();
        columnReferenceMatrix = new int[dimension][];
        for (int i = 0; i< columnReferenceMatrix.length; i++) {
            columnReferenceMatrix[i] = new int[dimension];
        }

        columnIdIndex = new HashMap<>();
        int index = 0;
        for (Integer columnId : columnIds) {
            columnIdIndex.putIfAbsent(columnId, index++);
        }

        tableReferenceGraph = new TableReferenceGraph(columnStatistics);
    }

    /**
     * Detect if adding the {@link InclusionDependencyInstance} would raise a conflict.
     * A conflict can be 1) uniqueness of foreign keys; 2) cyclic reference; 3) connected schema; 4) pk reference to pk
     * @param inclusionDependencyInstance
     * @return
     */
    public boolean detectConflicts(InclusionDependencyInstance inclusionDependencyInstance, List<UniqueColumnCombinationInstance> primaryKeys) {
        // is a cyclic reference raised
        boolean cyclicReference = this.detectCyclicReference(inclusionDependencyInstance);

        // same table one to one

        // is pk reference to pk
        boolean pk2pkReference = ForeignKeyPruningRules.isPKBelongsToPK(inclusionDependencyInstance, primaryKeys, null);

        // is adding this ind creating schema connectivity
        boolean schemaConnected = this.tableReferenceGraph.isConnected();
        return cyclicReference || pk2pkReference || schemaConnected;
    }

    public void addEdgeToColumnReferenceGraph(InclusionDependencyInstance inclusionDependencyInstance) {
        InclusionDependency inclusionDependency = inclusionDependencyInstance.getInd();
        int[] depColumnIds = inclusionDependency.getLhs().getColumnIds();
        int[] refColumnIds = inclusionDependency.getRhs().getColumnIds();

        for (int i=0;i<depColumnIds.length;i++) {
            this.columnReferenceMatrix[columnIdIndex.get(depColumnIds[i])][columnIdIndex.get(refColumnIds[i])] = 1;
        }
    }

    public boolean detectCyclicReference(InclusionDependencyInstance inclusionDependencyInstance) {
        InclusionDependency inclusionDependency = inclusionDependencyInstance.getInd();
        int[] depColumnIds = inclusionDependency.getLhs().getColumnIds();
        int[] refColumnIds = inclusionDependency.getRhs().getColumnIds();

        for (int i=0;i<depColumnIds.length;i++) {
            int depIndex = columnIdIndex.get(depColumnIds[i]);
            int refIndex = columnIdIndex.get(refColumnIds[i]);

            if (!hasCircle(depIndex, refIndex)) {
                return false;
            }
        }

        return true;
    }

    private boolean hasCircle(int depIndex, int refIndex) {
        Queue<Integer> queue = new LinkedList<>();
        int numberOfNodes = columnReferenceMatrix[0].length;
        columnReferenceMatrix[depIndex][refIndex] = 1;

        int[] visited = new int[numberOfNodes];
        int i, element;
        visited[depIndex] = 1;
        queue.add(depIndex);
        while (!queue.isEmpty()) {
            element = queue.remove();
            i = 0;
            while (i < numberOfNodes) {
                if (columnReferenceMatrix[element][i] == 1) {
                    if (i==depIndex) {
                        columnReferenceMatrix[depIndex][refIndex] = 0;
                        return true;
                    }
                    if (visited[i] == 0) {
                        queue.add(i);
                        visited[i] = 1;
                    }
                }
                i++;
            }
        }

        columnReferenceMatrix[depIndex][refIndex] = 0;
        return false;
    }

    public boolean isSchemaConnected(InclusionDependencyInstance inclusionDependencyInstance) {
        this.tableReferenceGraph.addEdgeToTableReferenceGraph(inclusionDependencyInstance);
        return this.tableReferenceGraph.isConnected();
    }

    public void removeEdgeFromTableReferenceGraph(InclusionDependencyInstance inclusionDependencyInstance) {
        this.tableReferenceGraph.removeEdgeToTableReferenceGraph(inclusionDependencyInstance);
    }
}

/**
 * The reference graph describes the reference relationships of predicted foreign keys. In the graph, a node represents a table in the relation,
 * whereas an edge represents an predicted foreign key 'LHS -> RHS' whose LHS and RHS are in the outbound and inbound nodes, respectively.
 *
 * @author Lan Jiang
 */
class TableReferenceGraph {

    /**
     * Internal undirected representation of the graph. the tableGraphMatrix is N*N, where N equals to the number of tables in the relation.
     * The value of the tableGraphMatrix cells is either 1 when there is an edge between the two tables, or 0 otherwise.
     *
     * Undirected graph is used to detect graph connectivity.
     */
    private final int[][] tableGraphMatrix;

    private final Map<Integer, Integer> tableIndex;

//    private Map<Integer, Table> tableOfColumn;
    private final Map<Integer, Integer> tableOfColumn;

    public TableReferenceGraph(Set<ColumnStatistics> columnStatistics) {
        Set<Integer> tableIds = columnStatistics.stream().map(ColumnStatistics::getTableId).collect(Collectors.toSet());
        tableIndex = new HashMap<>();
        int index = 0;
        for (Integer tableId : tableIds) {
            tableIndex.putIfAbsent(tableId, index++);
        }

        this.tableOfColumn = columnStatistics.stream().collect(Collectors.toMap(ColumnStatistics::getColumnId, ColumnStatistics::getTableId));

        this.tableGraphMatrix = new int[tableIds.size()][];
        for (int i = 0; i < tableIds.size(); i++) {
            this.tableGraphMatrix[i] = new int[tableIds.size()];
        }
    }

    /**
     * Add an edge to the undirected graph. The outbound and inbound nodes represent the tables that contain LHS and RHS columns of the inclusion dependency, respectively.
     */
    public void addEdgeToTableReferenceGraph(InclusionDependencyInstance inclusionDependencyInstance) {
        InclusionDependency inclusionDependency = inclusionDependencyInstance.getInd();
        int[] depColumnIds = inclusionDependency.getLhs().getColumnIds();
        int[] refColumnIds = inclusionDependency.getRhs().getColumnIds();
        int depTableId = tableOfColumn.get(depColumnIds[0]);
        int refTableId = tableOfColumn.get(refColumnIds[0]);
        tableGraphMatrix[tableIndex.get(depTableId)][tableIndex.get(refTableId)]++;
        tableGraphMatrix[tableIndex.get(refTableId)][tableIndex.get(depTableId)]++;
    }

    /**
     * Remove an edge from the undirected graph. The outbound and inbound nodes represent the tables that contain LHS and RHS columns of the inclusion dependency, respectively.
     */
    public void removeEdgeToTableReferenceGraph(InclusionDependencyInstance inclusionDependencyInstance) {
        InclusionDependency inclusionDependency = inclusionDependencyInstance.getInd();
        int[] depColumnIds = inclusionDependency.getLhs().getColumnIds();
        int[] refColumnIds = inclusionDependency.getRhs().getColumnIds();
        int depTableId = tableOfColumn.get(depColumnIds[0]);
        int refTableId = tableOfColumn.get(refColumnIds[0]);
        tableGraphMatrix[tableIndex.get(depTableId)][tableIndex.get(refTableId)]--;
        tableGraphMatrix[tableIndex.get(refTableId)][tableIndex.get(depTableId)]--;
    }

    public boolean isConnected() {
        Queue<Integer> queue = new LinkedList<>();
        int numberOfNodes = tableGraphMatrix.length;

        int[] visited = new int[numberOfNodes];
        int i, element;
        visited[0] = 1;
        queue.add(0);
        while (!queue.isEmpty()) {
            element = queue.poll();
            for (i = 0;i<numberOfNodes;i++) {
                if (tableGraphMatrix[element][i] == 1 && visited[i]==0) {
                    queue.add(i);
                    visited[i] = 1;
                }
            }
        }

        return Arrays.stream(visited).filter(integer -> integer==1).sum()==visited.length;
    }
}
