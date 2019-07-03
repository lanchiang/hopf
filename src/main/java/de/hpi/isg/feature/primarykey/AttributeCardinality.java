package de.hpi.isg.feature.primarykey;

import de.hpi.isg.target.UniqueColumnCombinationInstance;

import java.util.Collection;

/**
 * @author Lan Jiang
 */
public class AttributeCardinality extends PrimaryKeyFeature {

    @Override
    public void score(Collection<UniqueColumnCombinationInstance> uccInstances) {
        for (UniqueColumnCombinationInstance uccInstance : uccInstances) {
            int cardinality = uccInstance.getUcc().getArity();
            uccInstance.getFeatureScores().putIfAbsent(getClass().getSimpleName(), 1 / (double) cardinality);
        }
        normalize(uccInstances);
    }
}
