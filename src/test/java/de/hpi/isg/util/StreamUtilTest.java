package de.hpi.isg.util;

import de.hpi.isg.constraints.InclusionDependency;
import de.hpi.isg.element.InclusionDependencyInstance;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Lan Jiang
 * @since 2019-06-22
 */
public class StreamUtilTest {

    public static void main(String[] args) {
        InclusionDependencyInstance indInstance1 = new InclusionDependencyInstance(new InclusionDependency(1,1));
        indInstance1.setScore(0.5);
        InclusionDependencyInstance indInstance2 = new InclusionDependencyInstance(new InclusionDependency(2,2));
        indInstance2.setScore(0.2);
        InclusionDependencyInstance indInstance3 = new InclusionDependencyInstance(new InclusionDependency(3,3));
        indInstance3.setScore(0.4);
        InclusionDependencyInstance indInstance4 = new InclusionDependencyInstance(new InclusionDependency(4,4));
        indInstance4.setScore(0.8);
        InclusionDependencyInstance indInstance5 = new InclusionDependencyInstance(new InclusionDependency(5,5));
        indInstance5.setScore(0.6);

        Set<InclusionDependencyInstance> indInstances = new HashSet<>();
        indInstances.add(indInstance1);
        indInstances.add(indInstance2);
        indInstances.add(indInstance3);
        indInstances.add(indInstance4);
        indInstances.add(indInstance5);

        List<InclusionDependencyInstance> sortedIndInstances =
                indInstances.stream().sorted(Comparator.comparingDouble(InclusionDependencyInstance::getScore)).collect(Collectors.toList());
        sortedIndInstances.stream().forEachOrdered(indInstance -> System.out.println(indInstance));
    }
}
