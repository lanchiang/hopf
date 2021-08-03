package de.hpi.isg.util;

import com.beust.jcommander.Parameter;
import lombok.Getter;

/**
 * @author Lan Jiang
 */
public class Parameters {

    @Parameter(names = "--profile-basic-path", description = "The path that specifies the folder of all data profiles.", required = true)
    @Getter
    protected String profileBasicPath;

    @Parameter(names = "--data-path", description = "The path that specifies the folder of the dataset. Each table is sliced vertically into columns", required = true)
    @Getter
    protected String dataPath;

    @Parameter(names = "--null-string", description = "The representation of null. The default value is the empty string")
    @Getter
    protected String nullString = "";

    @Parameter(names = "--delimiter", description = "The delimiter of the relational table files. The default value is comma")
    @Getter
    protected String delimiter = ",";

    @Parameter(names = "--quote", description = "The quote character of the relational table files. The default value is double quote")
    @Getter
    protected String quoteCharacter = "\"";

    @Parameter(names = "--header", description = "Whether the data files are with headers.")
    @Getter
    protected boolean hasHeader = true;

    @Parameter(names = "--evaluate", description = "Evaluate the detected results against the ground truth.")
    @Getter
    protected boolean evaluate = true;
}
