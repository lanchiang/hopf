package de.hpi.isg.util;

/**
 * @author Lan Jiang
 * @since 11/7/19
 */
public class JavaStringUtils {

    /**
     * transform the string to java escaped string.
     * @param str
     * @return
     */
    public static String toJavaString(String str) {
        return str.replaceAll("\\\\\\\\", "\\\\\\\\\\\\\\\\");
    }
}
