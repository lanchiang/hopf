package de.hpi.isg.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lan Jiang
 */
public class StringUtils {

    /**
     * Order from right to left. For example, SalesOrderLineNumber -> 'Number','Line','Order','Sales'
     * @param columnName
     * @return
     */
    public static List<String> tokenize(String columnName) {
        String[] tokens = columnName.split("(?=\\p{Upper})");
        List<String> tokenList = new ArrayList<>();
        for (String token : tokens) {
            tokenList.add(0, token.toLowerCase());
        }
        return tokenList;
    }
}
