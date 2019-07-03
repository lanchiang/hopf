package de.hpi.isg.pruning;

import de.hpi.isg.constraints.InclusionDependency;
import de.hpi.isg.target.InclusionDependencyInstance;

import java.util.*;

/**
 * The conflict rules are used to prune inclusion dependencies that are impossible to be foreign keys.
 * Three pruning rules are proposed: 1) uniqueness of foreign keys; 2) non-cyclic reference; 3) schema connectivity.
 *
 *
 * @author Lan Jiang
 */
public class ConflictRules {

    private int[][] columnReferenceMatrix;
    private int dimension;
    private Map<Integer, Integer> columnIdIndex;

    private TableReferenceGraph tableReferenceGraph;

    public ConflictRules(List<Integer> columnIds) {
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
    }

    public void addEdgeToColumnReferenceGraph(InclusionDependencyInstance inclusionDependencyInstance) {
        InclusionDependency inclusionDependency = inclusionDependencyInstance.getInd();
        int[] depColumnIds = inclusionDependency.getLhs().getColumnIds();
        int[] refColumnIds = inclusionDependency.getRhs().getColumnIds();

        for (int i=0;i<depColumnIds.length;i++) {
            this.columnReferenceMatrix[columnIdIndex.get(depColumnIds[i])][columnIdIndex.get(refColumnIds[i])] = 1;
        }
    }

    public boolean circleConflict(InclusionDependencyInstance inclusionDependencyInstance) {
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

    public TableReferenceGraph getTableReferenceGraph() {
        return tableReferenceGraph;
    }
}
