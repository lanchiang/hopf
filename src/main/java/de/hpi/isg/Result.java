package de.hpi.isg;

import de.hpi.isg.element.InclusionDependencyInstance;
import de.hpi.isg.element.UniqueColumnCombinationInstance;

import java.util.List;

/**
 * The results class represents the predicted primary keys and foreign keys.
 *
 * @author Lan Jiang
 */
public class Result {

    private List<UniqueColumnCombinationInstance> primaryKeys;

    private List<InclusionDependencyInstance> foreignKeys;

    public Result(List<UniqueColumnCombinationInstance> primaryKeys, List<InclusionDependencyInstance> foreignKeys) {
        this.primaryKeys = primaryKeys;
        this.foreignKeys = foreignKeys;
    }

    public List<UniqueColumnCombinationInstance> getPrimaryKeys() {
        return primaryKeys;
    }

    public void setPrimaryKeys(List<UniqueColumnCombinationInstance> primaryKeys) {
        this.primaryKeys = primaryKeys;
    }

    public List<InclusionDependencyInstance> getForeignKeys() {
        return foreignKeys;
    }

    public void setForeignKeys(List<InclusionDependencyInstance> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }
}
