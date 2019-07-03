package de.hpi.isg.element;

import de.hpi.isg.constraints.UniqueColumnCombination;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lan Jiang
 */
public class UniqueColumnCombinationInstance {

    private Map<String, Object> featureVector;

    /**
     * The score of each individual feature.
     */
    private Map<String, Double> featureScores;

    /**
     * The underlying {@link UniqueColumnCombination}.
     */
    private UniqueColumnCombination ucc;

    /**
     * The table to which this ucc belongs.
     */
    private Table belongedTable;

    /**
     * Primary key score for this unique column combination.
     */
    private double score;

    /**
     * Whether this unique column combination is a true primary key.
     */
    private Result isPrimaryKey;

    public UniqueColumnCombinationInstance(UniqueColumnCombination ucc) {
        this.isPrimaryKey = Result.UNKNOWN;
        this.featureVector = new HashMap<>();
        this.featureScores = new HashMap<>();
        this.ucc = ucc;
    }

    public Map<String, Double> getFeatureScores() {
        return featureScores;
    }

    public UniqueColumnCombination getUcc() {
        return ucc;
    }

    public Table getBelongedTable() {
        return belongedTable;
    }

    public Result getIsPrimaryKey() {
        return isPrimaryKey;
    }

    public void setIsPrimaryKey(Result isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }

    public enum Result {
        PRIMARY_KEY,

        UNKNOWN,

        NO_PRIMARY_KEY,
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
