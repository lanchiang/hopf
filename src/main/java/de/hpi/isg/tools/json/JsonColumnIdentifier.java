package de.hpi.isg.tools.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * @author Lan Jiang
 */
public class JsonColumnIdentifier {

    @JsonProperty("tableIdentifier")
    @Getter protected String tableIdentifier;
    @JsonProperty("columnIdentifier")
    @Getter protected String columnIdentifier;
}
