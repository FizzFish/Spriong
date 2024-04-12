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
        SootWorld sootWorld = SootWorld.v();
        sootWorld.readConfig(args[0]);
        sootWorld.loadJar(args[1]);
        SootMethod entryMethod = sootWorld.getEntryMethod();
        sootWorld.visitMethod(entryMethod);
    }
}






