package org.lambd.utils;

import soot.SootField;

import java.util.List;
import java.util.stream.Collectors;

public class Utils {
    public static final SootField arrayField = new SootField("[*]", soot.RefType.v("java.lang.String"));
    public static String argString(int i)
    {
        if (i == -1)
            return "this";
        if (i == -2)
            return "ret";
        return "arg" + i;
    }
    public static String fieldString(List<SootField> fields)
    {
        if (fields.isEmpty())
            return "";
        return fields.stream().map(SootField::getName).collect(Collectors.joining("."));
    }
    public static String concat(String s1, String s2) {
        return  (s1 + "." + s2).replaceAll("^\\.*|\\.*$", "");
    }
}
