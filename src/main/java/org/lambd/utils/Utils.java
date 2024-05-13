package org.lambd.utils;

import java.util.List;

public class Utils {
    public static final String arrayStr = "[*]";
    public static String argString(int i)
    {
        if (i == -1)
            return "this";
        if (i == -2)
            return "ret";
        return "arg" + i;
    }
    public static String fieldString(List<String> fields)
    {
        if (fields.isEmpty())
            return "";
        return String.join(".", fields);
    }
    public static String concat(String s1, String s2) {
        return  (s1 + "." + s2).replaceAll("^\\.*|\\.*$", "");
    }
}
