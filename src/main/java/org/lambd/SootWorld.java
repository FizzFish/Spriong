package org.lambd;

import org.lambd.transition.MethodSummary;
import org.lambd.transition.TaintConfig;
import org.lambd.transition.Transition;
import org.lambd.utils.ClassNameExtractor;
import soot.*;
import soot.jimple.*;
import soot.options.Options;
import soot.util.Chain;

import java.util.*;
import java.util.stream.Collectors;

public class SootWorld {
    private SootMethod entryMethod = null;
    private static SootWorld world = null;
    private List<String> sourceInfo = new ArrayList<>();
    private Map<String, List<Transition>> methodRefMap = new HashMap<>();
    private Map<SootMethod, SpMethod> methodMap = new HashMap<>();
    private SootWorld() {
    }
    public void readConfig(String config) {
        String path = String.format("src/main/resources/%s", config);
        new TaintConfig(path).parse(methodRefMap, sourceInfo);
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
    public void initSoot(String jars) {
        G.reset();
        String sourceDir = "src/main/resources/";
        String[] parts = jars.split(";");
        List<String> inputClasses = new ArrayList<>();
        for (int i = 0; i < parts.length; i++) {
            parts[i] = sourceDir.concat(parts[i]);
            inputClasses.addAll(ClassNameExtractor.extract(parts[i]));
        }
        String sootCp = String.format("src/main/resources/rt.jar;%s", String.join(";", parts));
        Options.v().set_soot_classpath(sootCp);


        Options.v().set_whole_program(true);
        soot.options.Options.v().set_output_format(soot.options.Options.output_format_jimple);
        soot.options.Options.v().set_app(true);
        soot.options.Options.v().set_allow_phantom_refs(true);
        soot.options.Options.v().set_no_bodies_for_excluded(true);
        soot.options.Options.v().set_exclude(Arrays.asList("java.*", "javax.*", "sun.*", "jdk.*", "com.sun.*"));
        Options.v().set_verbose(true);

        for (String className : inputClasses) {
            SootClass sootClass = Scene.v().loadClassAndSupport(className);
            sootClass.setApplicationClass(); // 将类标记为应用程序类
        }
//        SootClass entryClass = Scene.v().loadClassAndSupport(sourceInfo.get(0));
        Scene.v().loadNecessaryClasses();
//        soot.options.Options.v().setPhaseOption("cg.cha", "on");
        soot.options.Options.v().setPhaseOption("cg", "all-reachable:true");
        Options.v().setPhaseOption("cg.spark", "on");
        Options.v().setPhaseOption("cg.spark", "rta:true");  // 开启RTA
        Options.v().setPhaseOption("cg.spark", "on-fly-cg:false"); // 禁用即时调用图生成

        soot.options.Options.v().setPhaseOption("jb", "use-original-names:true");
//        soot.options.Options.v().setPhaseOption("jb.ls", "enabled:false");
//        Options.v().set_prepend_classpath(false);
        PackManager.v().runPacks();
        // 遍历所有类和方法
        Chain<SootClass> classes = Scene.v().getClasses();
        System.out.println("Classes size: " + classes.size());
        this.entryMethod = Scene.v().getMethod(sourceInfo.get(1));
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
        spMethod.getSummary().print(true);
    }
    public SpMethod getMethod(SootMethod method) {
        if (methodMap.containsKey(method))
            return methodMap.get(method);
        return new SpMethod(method);
    }
}
