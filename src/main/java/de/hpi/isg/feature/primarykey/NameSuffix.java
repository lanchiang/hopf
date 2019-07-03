package de.hpi.isg.feature.primarykey;

import de.hpi.isg.constraints.UniqueColumnCombination;
import de.hpi.isg.element.UniqueColumnCombinationInstance;
import de.hpi.isg.util.StringUtils;

import java.util.*;

/**
 * @author Lan Jiang
 */
public class NameSuffix extends PrimaryKeyFeature {

    // case insensitive
    private final static String[] suffixKeywords = new String[]{"id","key","nr","no","number"};

    private Map<Integer, String> columnNameByColumnId;

    public NameSuffix() {
        this.columnNameByColumnId = new HashMap<>();
        // Todo: fill the columnNameByColumnId map
    }

    @Override
    public void score(Collection<UniqueColumnCombinationInstance> uccInstances) {
        for (UniqueColumnCombinationInstance uccInstance : uccInstances) {
            UniqueColumnCombination uniqueColumnCombination = uccInstance.getUcc();
            int[] columnIds = uniqueColumnCombination.getColumnCombination().getColumnIds();

            double overall = 0;
            for (int columnId : columnIds) {
                String columnName = columnNameByColumnId.get(columnId);
                List<String> tokens = StringUtils.tokenrize(columnName);
                double numLeftOverTokens = tokens.size();
                double nameSuffixScore = 0.0;
                if (endWithCount(columnName)) {
                    nameSuffixScore = 1.0;
                    numLeftOverTokens = tokens.size()-1;
                }
                if (numLeftOverTokens==0) {
                    numLeftOverTokens = 1;
                }
                double nameLengthScore = 1.0 / numLeftOverTokens;
                double score = (nameLengthScore+nameSuffixScore)/2.0;
                overall += score;
            }
            overall /= (double)columnIds.length;
            uccInstance.getFeatureScores().putIfAbsent(getClass().getSimpleName(), overall);
        }

        normalize(uccInstances);
    }

    private boolean endWithCount(String columnName) {
        for (String suffix : suffixKeywords) {
            if (columnName.toLowerCase().endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }
}
