package de.hpi.isg.feature.foreignkey;

import de.hpi.isg.element.Column;
import de.hpi.isg.element.InclusionDependencyInstance;
import de.hpi.isg.util.MathUtils;
import de.hpi.isg.util.StringUtils;

import java.util.*;

/**
 * @author Lan Jiang
 */
public class AttributeNameSimilarity extends ForeignKeyFeature {

    private final Map<Integer, List<String>> tokensByIndex;

    private final Map<Integer, Integer> tableIdByColumnId;

    public AttributeNameSimilarity(List<Column> columns) {
        this.tokensByIndex = new HashMap<>();

        columns.forEach(column -> {
            tokensByIndex.putIfAbsent(column.getColumnId(), StringUtils.tokenize(column.getColumnName()));
            tokensByIndex.putIfAbsent(column.getTableId(), StringUtils.tokenize(column.getTableName()));
        });

        this.tableIdByColumnId = new HashMap<>();
        columns.forEach(column -> tableIdByColumnId.putIfAbsent(column.getColumnId(), column.getTableId()));
    }

    @Override
    public void score(Collection<InclusionDependencyInstance> indInstances) {
        for (InclusionDependencyInstance indInstance: indInstances) {
            final int[] depColumnIds = indInstance.getInd().getLhs().getColumnIds();
            final int[] refColumnIds = indInstance.getInd().getRhs().getColumnIds();
            final int depTableId = tableIdByColumnId.get(depColumnIds[0]);
            final int refTableId = tableIdByColumnId.get(refColumnIds[0]);

            double overallScore = 0d;
            for (int i=0;i<depColumnIds.length;i++) {
                List<String> depColumnTokens = new ArrayList<>(tokensByIndex.get(depColumnIds[i]));
                List<String> refColumnTokens = new ArrayList<>(tokensByIndex.get(refColumnIds[i]));
                List<String> depTableTokens = new ArrayList<>(tokensByIndex.get(depTableId));
                List<String> refTableTokens = new ArrayList<>(tokensByIndex.get(refTableId));

                depColumnTokens.addAll(depTableTokens);
                refColumnTokens.addAll(refTableTokens);

                Map<String, Map<String, Double>> scores = new HashMap<>();
                for (String refColumnToken: refColumnTokens) {
                    scores.putIfAbsent(refColumnToken, new HashMap<>());
                    for (String depColumnToken : depColumnTokens) {
                        scores.get(refColumnToken).putIfAbsent(depColumnToken, MathUtils.levenshtein(refColumnToken, depColumnToken));
                    }
                }

                double numerator = 0d;
                double denominator = 0d;

                while (!refColumnTokens.isEmpty()) {
                    double maxRefTokenScore = 0d;
                    int indexRefToken = 0, indexDepToken = 0;
                    double frequency;
                    String optimalRefToken = refColumnTokens.get(indexRefToken);

                    if (!depColumnTokens.isEmpty()) {
                        for (int index = 0; index < refColumnTokens.size(); index++) {
                            String refToken = refColumnTokens.get(index);
                            int depIndex = indexOfMostSimilar(depColumnTokens, refToken);
                            double currentScore = scores.get(refToken).get(depColumnTokens.get(depIndex));
                            if (maxRefTokenScore < currentScore) {
                                maxRefTokenScore = currentScore;
                                optimalRefToken = refToken;
                                indexRefToken = index;
                                indexDepToken = depIndex;
                            }
                        }
                        final String thisRefToken = optimalRefToken;
                        frequency = tokensByIndex.entrySet().stream()
                                .filter(entry -> entry.getValue().contains(thisRefToken)).count();
                        depColumnTokens.remove(indexDepToken);
                    } else {
                        final String thisRefToken = optimalRefToken;
                        frequency = tokensByIndex.entrySet().stream()
                                .filter(entry -> entry.getValue().contains(thisRefToken)).count();
                    }
                    frequency = frequency / tokensByIndex.size();
                    numerator += maxRefTokenScore * Math.log(1 / frequency);
                    denominator += Math.log(1 / frequency);

                    refColumnTokens.remove(indexRefToken);
                }

                double score = numerator / denominator;
                overallScore += score;
            }
            overallScore /= depColumnIds.length;

            indInstance.getFeatureScores().putIfAbsent(getClass().getSimpleName(), overallScore);
        }

        normalize(indInstances);
    }

    /**
     * Get the index of the string in the depTokens that is most similar regarding levenshtein measurement to the given refToken.
     *
     * @param depTokens
     * @param refToken
     * @return the index of the string.
     */
    private int indexOfMostSimilar(List<String> depTokens, String refToken) {
        double highest = 0.0;
        int index = 0;
        for (int i=0;i<depTokens.size();i++) {
            String depToken = depTokens.get(i);
            double sim = MathUtils.levenshtein(depToken, refToken);
            if (sim>highest) {
                highest = sim;
                index = i;
            }
        }
        return index;
    }
}
