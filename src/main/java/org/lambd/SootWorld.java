package org.lambd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class SootWorld {
    private static final Logger logger = LogManager.getLogger(SootWorld.class);
    public SootMethod entryMethod = null;
    public SootWorld() {}
    public void loadJar(String jarPath) {
        G.reset();
        String sootCp = String.format("src/main/resources/rt.jar;src/main/resources/%s", jarPath);
        Options.v().set_soot_classpath(sootCp);
        System.out.println(sootCp);
        String entryClassName = "org.apache.commons.text.StringSubstitutor";
        String entryMethodName = "java.lang.String replace(java.lang.String)";
        // 配置Soot
        Options.v().set_whole_program(true);
        soot.options.Options.v().set_output_format(
                soot.options.Options.output_format_jimple);
        soot.options.Options.v().set_app(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_exclude(Arrays.asList("java.*", "javax.*", "sun.*", "jdk.*", "com.sun.*"));
        // 如果没有明确的入口点，可以考虑使用Scene.v().setEntryPoints()来设置应用的入口点；
        // 这里我们手动指定入口点
        SootClass entryClass = Scene.v().loadClassAndSupport(entryClassName);
        Scene.v().loadNecessaryClasses();
//        entryClass.setApplicationClass();
        SootMethod entryMethod = entryClass.getMethod(entryMethodName);
        List<SootMethod> entryPoints = new ArrayList<>();
        entryPoints.add(entryMethod);
        this.entryMethod = entryMethod;
        Scene.v().setEntryPoints(entryPoints);

        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().setPhaseOption("jb", "use-original-names:true");
        Options.v().setPhaseOption("jb.ls", "enabled:false");
        Options.v().set_prepend_classpath(false);

        // 运行Soot分析
        PackManager.v().runPacks();

        // 获取并处理调用图
        CallGraph cg = Scene.v().getCallGraph();
//        System.out.printf("Call Graph: %s\n", cg.toString());
    }

    public void showCallee(SootMethod method) {
        CallGraph cg = Scene.v().getCallGraph();


        for (Iterator<Edge> it = cg.edgesOutOf(method); it.hasNext();) {
            Edge edge = it.next();
            SootMethod callee = edge.tgt(); // 获取被调用的方法

            System.out.println("Callee of " + edge.srcStmt()+ ": " + callee.getName());
        }
    }

    public void showStmts(SootMethod method) {
        for (Iterator<Unit> it = method.getActiveBody().getUnits().iterator(); it.hasNext();) {
            Unit unit = it.next();
            Transition transition = null;
            if (unit instanceof IdentityStmt identityStmt) {
                Local lhs = (Local) identityStmt.getLeftOp();
                Value val = identityStmt.getRightOp();
                if (val instanceof ParameterRef parameterRef) {
                    int index = parameterRef.getIndex();
                    transition = new OutSideTransition(index, true, lhs);
                } else if (val instanceof ThisRef) {
                    transition = new OutSideTransition(-1, true, lhs);
                }
            } else if (unit instanceof InvokeStmt invokeStmt) {
                transition = new InvokeTransition(unit);
            } else if (unit instanceof AssignStmt assignStmt) {
                Local lhs = (Local) assignStmt.getLeftOp();
                Value rhs = assignStmt.getRightOp();
                if (rhs instanceof InvokeExpr invokeExpr) {
                    transition = new InvokeTransition(unit);

                } else if (rhs instanceof NewExpr newExpr) {
                    transition = new NewTransition(lhs, newExpr.getType());
                }
            } else if (unit instanceof ReturnStmt returnStmt) {
                Value retVal = returnStmt.getOp();
                if (retVal instanceof Local local) {
                    transition = new OutSideTransition(-2, false, local);
                }
            }
            if (transition != null) {
                System.out.println(transition);
            }
        }
    }
}
