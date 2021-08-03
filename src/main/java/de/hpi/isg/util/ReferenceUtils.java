package de.hpi.isg.util;

/**
 * @author Lan Jiang
 */
public class ReferenceUtils {

    public static boolean isSorted(int[] ids) {
        for (int i = 1; i < ids.length; i++) {
            if (ids[i - 1] > ids[i]) return false;
        }
        return true;
    }
}
