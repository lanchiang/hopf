package de.hpi.isg.element;

import de.hpi.isg.constraints.InclusionDependency;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lan Jiang
 */
public class InclusionDependencyInstance {

    /**
     * Used to decide whether a IND feature vector is a foreign key.
     */
    private Result isForeignKey;

    /**
     * Represents the feature vector of a instance.
     */
    private Map<String, Double> featureScores;

    private InclusionDependency ind;

    private double score;

    public InclusionDependencyInstance(InclusionDependency ind) {
        this.isForeignKey = Result.UNKNOWN;
        this.featureScores = new HashMap<>();
        this.ind = ind;
    }

    public Map<String, Double> getFeatureScores() {
        return featureScores;
    }

    public InclusionDependency getInd() {
        return ind;
    }

    public Result getIsForeignKey() {
        return isForeignKey;
    }

    public void setIsForeignKey(Result isForeignKey) {
        this.isForeignKey = isForeignKey;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    /**
     * Possible results of a partial foreign key classifier.
     */
    public enum Result {
        /**
         * Indicates that the classifier believes that an IND is a foreign key.
         */
        FOREIGN_KEY,

        /**
         * Indicates that the classifier is not sure whether an IND is a foreign key.
         */
        UNKNOWN,

        /**
         * Indicates that the classifier believes that an IND is not a foreign key.
         */
        NO_FOREIGN_KEY;
    }

    @Override
    public String toString() {
        return "InclusionDependencyInstance{" +
                "ind=" + ind +
                ", score=" + score +
                '}';
    }
}
