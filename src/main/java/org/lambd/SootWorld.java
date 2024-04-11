package org.lambd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lambd.transition.MethodSummary;
import org.lambd.transition.SinkTransition;
import org.lambd.transition.TaintConfig;
import org.lambd.transition.Transition;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;

import java.util.*;

public class SootWorld {
    private static final Logger logger = LogManager.getLogger(SootWorld.class);
    private SootMethod entryMethod = null;
    private static SootWorld world = null;
    private String entryMethodName;
    private Map<String, List<Transition>> methodRefMap = new HashMap<>();
    private Map<SootMethod, SpMethod> methodMap = new HashMap<>();
    private SootWorld() {
        entryMethodName = new TaintConfig("src/main/resources/transfer.yml").parse(methodRefMap);
    }
    public static SootWorld v() {
        if (world == null) {
            world = new SootWorld();
        }
        return world;
    }
    public SootMethod getEntryMethod() {
        return entryMethod;
    }
    public void loadJar(String jarPath) {
        G.reset();
        String sootCp = String.format("src/main/resources/rt.jar;src/main/resources/%s", jarPath);
        Options.v().set_soot_classpath(sootCp);
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

        Options.v().setPhaseOption("cg.cha", "on");
        Options.v().setPhaseOption("cg", "all-reachable:true");
        Options.v().setPhaseOption("jb", "use-original-names:true");
        Options.v().setPhaseOption("jb.ls", "enabled:false");
        Options.v().set_prepend_classpath(false);

        // 运行Soot分析
        PackManager.v().runPacks();
    }

    List<SootMethod> visited = new ArrayList<>();
    public List<SootMethod> getVisited() {
        return visited;
    }

    public boolean quickMethodRef(String signature, SpMethod caller, Stmt stmt) {
        // 1. transition or sink not enter
        if (methodRefMap.containsKey(signature)) {
            methodRefMap.get(signature).forEach(transition -> {
                transition.apply(caller, stmt);
            });
            return true;
        }
        return false;
    }
    public boolean quickCallee(SootMethod callee, SpMethod caller, Stmt stmt) {
        // 1. transition or sink not enter
        MethodSummary summary = getMethod(callee).getSummary();
        if (!summary.isEmpty()) {
            summary.apply(caller, stmt);
            return true;
        }
        return false;
    }

    public void visitMethod(SootMethod method) {
        if (!method.getDeclaringClass().isApplicationClass() || visited.contains(method))
            return;
        visited.add(method);
        SpMethod spMethod = new SpMethod(method);
        methodMap.put(method, spMethod);
        StmtVisitor visitor = new StmtVisitor(spMethod);
        for (Iterator<Unit> it = method.getActiveBody().getUnits().iterator(); it.hasNext();) {
            Unit unit = it.next();
            visitor.visit((Stmt) unit);
        }
        if (!spMethod.getSummary().isEmpty())
            spMethod.getSummary().print();
    }
    public SpMethod getMethod(SootMethod method) {
        if (methodMap.containsKey(method))
            return methodMap.get(method);
        return new SpMethod(method);
    }
}
