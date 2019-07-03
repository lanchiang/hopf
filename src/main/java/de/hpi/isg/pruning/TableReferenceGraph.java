package de.hpi.isg.pruning;

import de.hpi.isg.constraints.InclusionDependency;
import de.hpi.isg.element.InclusionDependencyInstance;
import de.hpi.isg.element.Table;

import java.util.*;

/**
 * The reference graph describes the reference relationships of predicted foreign keys. In the graph, a node represents a table in the relation,
 * whereas an edge represents an predicted foreign key 'LHS -> RHS' whose LHS and RHS are in the outbound and inbound nodes, respectively.
 *
 * @author Lan Jiang
 */
public class TableReferenceGraph {

    /**
     * Internal undirected representation of the graph. the tableGraphMatrix is N*N, where N equals to the number of tables in the relation.
     * The value of the tableGraphMatrix cells is either 1 when there is an edge between the two tables, or 0 otherwise.
     *
     * Undirected graph is used to detect graph connectivity.
     */
    private int[][] tableGraphMatrix;

    private Map<Integer, Integer> tableIndex;

    private Map<Integer, Table> tableOfColumn;

    public TableReferenceGraph(Map<Integer, Table> tableOfColumn, List<Table> tables) {
        int tableSize = tables.size();
        tableGraphMatrix = new int[tableSize][];
        for (int i=0;i<tableSize;i++) {
            tableGraphMatrix[i] = new int[tableSize];
        }

        this.tableOfColumn = tableOfColumn;
        tableIndex = new HashMap<>();
        int index = 0;
        for (Table table : tables) {
            tableIndex.putIfAbsent(table.getTableId(), index++);
        }
    }

    /**
     * Add an edge to the undirected graph. The outbound and inbound nodes represent the tables that contain LHS and RHS columns of the inclusion dependency, respectively.
     */
    public void addEdgeToTableReferenceGraph(InclusionDependencyInstance inclusionDependencyInstance) {
        InclusionDependency inclusionDependency = inclusionDependencyInstance.getInd();
        int[] depColumnIds = inclusionDependency.getLhs().getColumnIds();
        int[] refColumnIds = inclusionDependency.getRhs().getColumnIds();
        int depTableId = tableOfColumn.get(depColumnIds[0]).getTableId();
        int refTableId = tableOfColumn.get(refColumnIds[0]).getTableId();
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
        int depTableId = tableOfColumn.get(depColumnIds[0]).getTableId();
        int refTableId = tableOfColumn.get(refColumnIds[0]).getTableId();
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
