package de.hpi.isg.tools.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

/**
 * @author Lan Jiang
 */
public class JsonColumnCombination {

    @JsonProperty("columnIdentifiers")
    @Getter protected List<JsonColumnIdentifier> columnIdentifiers;
}
