package org.lambd;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.util.Chain;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {

        SootWorld.v().loadJar("commons-text-1.8.jar");
        SootMethod entryMethod = SootWorld.v().getEntryMethod();
        SootWorld.v().visitMethod(entryMethod);
    }
}






