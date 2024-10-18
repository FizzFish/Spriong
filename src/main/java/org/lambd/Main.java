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
        long startTime = System.currentTimeMillis();
        SootWorld sootWorld = SootWorld.v();
        sootWorld.readConfig(args[0]);
//        sootWorld.initSoot(args[1]);
        sootWorld.driverAnalysis(args[1]);
        SootMethod entryMethod = sootWorld.getEntryMethod();
        sootWorld.analyze(entryMethod);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("Execution time: " + duration + " milliseconds");
        sootWorld.statistics();
    }
}






