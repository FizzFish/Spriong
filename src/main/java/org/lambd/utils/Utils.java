package org.lambd.utils;

public class Utils {
    public static String argString(int i)
    {
        if (i == -1)
            return "this";
        if (i == -2)
            return "ret";
        return "arg" + i;
    }
}
