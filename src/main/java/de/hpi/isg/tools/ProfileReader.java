package de.hpi.isg.tools;

/**
 * @author Lan Jiang
 */
abstract public class ProfileReader {

    protected static final String TABLE_MARKER = "# TABLES";
    protected static final String COLUMN_MARKER = "# COLUMN";
    protected static final String RESULT_MARKER = "# RESULTS";

    protected static final String TABLE_COLUMN_SEPARATOR = "\\.";
    protected static final String MAPPING_SEPARATOR = "\t";
    protected static final String CC_RESULT_SEPARATOR = ",";
    protected static final String BELONGINGTO_MARKER = "[=";

    public abstract void processLine(String line);
}
