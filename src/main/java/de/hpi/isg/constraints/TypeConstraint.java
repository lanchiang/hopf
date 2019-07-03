package de.hpi.isg.constraints;

/**
 * @author Lan Jiang
 */
public class TypeConstraint implements Constraint {

    private static final long serialVersionUID = 3194245498846860560L;

    private final String type;

    private final int columnId;

    public TypeConstraint(final int columnId, String type) {
        this.columnId = columnId;
        this.type = type;
    }

    public int getColumnId() {
        return this.columnId;
    }

    public String getType() {
        return type;
    }
}
