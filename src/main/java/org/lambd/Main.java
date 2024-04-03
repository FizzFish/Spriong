package org.lambd;

import soot.Scene;
import soot.SootClass;
import soot.util.Chain;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {

        System.out.println("Hello world!");
        SootWorld sootWorld = new SootWorld();
        sootWorld.loadJar("commons-text-1.8.jar");
        List<SootClass> applicationClasses = Scene.v().getClasses().stream()
                .filter(SootClass::isApplicationClass).collect(Collectors.toList());
        System.out.println(applicationClasses.size());
//        applicationClasses.forEach(sootClass -> {
//            System.out.println(sootClass.getName());
//            sootClass.getMethods().forEach(method -> {
//                System.out.println(method.getSignature());
//            });
//        });
//        sootWorld.showCallee(sootWorld.entryMethod);
        sootWorld.showStmts(sootWorld.entryMethod);
    }
}






