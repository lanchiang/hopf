package de.hpi.isg.element;

import de.hpi.isg.constraints.UniqueColumnCombination;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lan Jiang
 */
public class UniqueColumnCombinationInstance {

    @Getter
    private Map<String, Object> featureVector;

    /**
     * The score of each individual feature.
     */
    @Getter
    private Map<String, Double> featureScores;

    /**
     * The underlying {@link UniqueColumnCombination}.
     */
    @Getter
    private UniqueColumnCombination ucc;

    /**
     * The table to which this ucc belongs.
     */
    @Getter
    private Table belongedTable;

    /**
     * Primary key score for this unique column combination.
     */
    @Getter @Setter
    private double score;

    /**
     * Whether this unique column combination is a true primary key.
     */
    @Getter @Setter
    private Result isPrimaryKey;

    public UniqueColumnCombinationInstance(UniqueColumnCombination ucc) {
        this.isPrimaryKey = Result.UNKNOWN;
        this.featureVector = new HashMap<>();
        this.featureScores = new HashMap<>();
        this.ucc = ucc;

        this.belongedTable = new Table(ucc.getColumnCombination().getTableId(), ucc.getColumnCombination().getColumnIds());
    }

    public enum Result {
        PRIMARY_KEY,

        UNKNOWN,

        NO_PRIMARY_KEY,
    }
}
