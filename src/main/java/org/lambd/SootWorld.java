package org.lambd;

import org.lambd.annotation.AutoWired;
import org.lambd.transition.*;
import org.lambd.utils.ClassNameExtractor;
import org.lambd.wrapper.SpSootClass;
import soot.*;
import soot.jimple.*;
import soot.options.Options;

import java.io.File;
import java.util.*;

/**
 * 1.通过soot分析所有相关的jar包
 * 2.从入口方法开始，分析所有方法visitMethod
 */
public class SootWorld {
    private SootMethod entryMethod = null;
    public SootClass entryClass = null;
    private static SootWorld world = null;
    private Config config = null;
    private Map<String, List<Transition>> methodRefMap = new HashMap<>();
    private Map<SootMethod, SpMethod> methodMap = new HashMap<>();
    private Map<SootClass, SpSootClass> classMap = new HashMap<>();
    private Queue<SootMethod> entryPoints = new LinkedList<>();
    private AutoWired autoWired = new AutoWired();
    private Set<SpMethod> updateCallers = new HashSet<>();
    private NeoGraph graph;
    private SootWorld() {
        graph = new NeoGraph("bolt://localhost:7687", "neo4j", "123456", "neo4j",false);
    }
    public void setConfig(Config config) {
        this.config = config;
        config.transfers.forEach(transfer -> {
            List<Transition> transitions = new ArrayList<>();
            for (Object obj : transfer.transitions) {
                Map item = (Map) obj;
                if (item.containsKey("from")) {
                    Transition transition = new BaseTransition((Integer) item.get("from"),
                            (Integer) item.get("to"), (Integer) item.get("kind"));
                    transitions.add(transition);
                } else if (item.containsKey("code")) {
                    String code = (String) item.get("code");
                    Transition transition = new LoadTransition(code);
                    transitions.add(transition);
                }
            }
            methodRefMap.put(transfer.method, transitions);
        });
        config.sinks.forEach(sink -> {
            List<Transition> sinkList = new ArrayList<>();
            sinkList.add(new SinkTrans(sink.method, sink.index, Weight.ONE));
            methodRefMap.put(sink.method, sinkList);
        });
    }
    public static SootWorld v() {
        if (world == null) {
            world = new SootWorld();
        }
        return world;
    }

    List<SootMethod> visited = new ArrayList<>();
    public List<SootMethod> getVisited() {
        return visited;
    }
    public void addTransition(String signature, Transition transition) {
        methodRefMap.computeIfAbsent(signature, k -> new ArrayList<Transition>()).add(transition);
    }
    public AutoWired getAutoWired() {
        return autoWired;
    }
    public boolean quickMethodRef(String signature, SpMethod caller, Stmt stmt) {
        // 1. transition or sink not enter
        // authenticate or some transport callback
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
        Summary summary = getMethod(callee).getSummary();
        if (!summary.isEmpty()) {
            summary.apply(caller, stmt);
            return true;
        }
        return false;
    }
    private boolean check(SootMethod method, String name, String cls) {
        if (method.getName().equals(name) && method.getDeclaringClass().getShortName().equals(cls))
            return true;
        return false;
    }
    public void addActiveEdge(SootMethod callee, SpMethod caller) {
        SpMethod spCallee = getMethod(callee);
        if (!spCallee.isFinished()) {
            spCallee.getSummary().addCaller(caller);
        }
    }
    public void visitMethod(SootMethod method, Map<Integer, Set<SootClass>> mayClassMap) {
        if (!method.getDeclaringClass().isApplicationClass() || visited.contains(method))
            return;
        visited.add(method);
        SpMethod spMethod = getMethod(method);
        spMethod.visit();
        if (mayClassMap != null) {
            mayClassMap.forEach((index, mayClasses) -> {
                for (SootClass sc : mayClasses)
                    spMethod.addMayClass(index, sc);
            });
        }

        StmtVisitor visitor = new StmtVisitor(spMethod);
        if (method.hasActiveBody()) {
            for (Unit unit : method.getActiveBody().getUnits()) {
                visitor.visit((Stmt) unit);
            }
        }
        spMethod.getSummary().print(true);
        spMethod.finish();
        graph.updateMethodSummary(spMethod);
    }
    public void updateNeo4jRelation(SootMethod caller, SootMethod callee) {
        graph.createRelationWithMethods(caller, callee);
    }
    public SpMethod getMethod(SootMethod method) {
        return methodMap.computeIfAbsent(method, k -> new SpMethod(method));
    }
    public SpSootClass getClass(SootClass cls) {
        return classMap.computeIfAbsent(cls, k -> new SpSootClass(cls));
    }
    public void statistics() {
        int varSize = 0, objSize = 0;
        for (SootMethod method : visited) {
            varSize += methodMap.get(method).getPtset().getVarSize();
            objSize += methodMap.get(method).getPtset().getObjSize();
        }
        System.out.printf("there are %d functions, %d vars, %d objs\n",
                visited.size(), varSize, objSize);
    }
    public void analyze() {
        while (!entryPoints.isEmpty()) {
            SootMethod sm = entryPoints.poll();
            System.out.println("Analyze Entry: " + sm);
            visitMethod(sm, null);
        }
//        if(!updateCallers.isEmpty()) {
//            Set<SpMethod> copySet = new HashSet<>(updateCallers);
//            updateCallers.clear();
//            Iterator<SpMethod> iterator = copySet.iterator();
//            while (iterator.hasNext()) {
//                SpMethod element = iterator.next();
//                iterator.remove();
//                visitMethod(element.getSootMethod(), true);
//                System.out.println("Polled Element: " + element);
//            }
//        }
        System.out.println("Analyze end, update Neo4j database");
        graph.flush();
    }

    public void analyzePackage(Set<String> packageName) {
        //analyze all app classes at beginning
        /**
        List<SootClass> classes = new ArrayList<>();
        packageName.forEach(pkg -> {
            Scene.v().getApplicationClasses().forEach(sc -> {
                if (sc.getPackageName().startsWith(pkg))
                    classes.add(sc);

            });
        });
        // Scene.v().loadNecessaryClasses();
        System.out.println(classes);
        for (SootClass sc: classes)
            autoWired.scanMethodInClass(sc);
         */
    }

    public void addCaller(SpMethod caller) {
        updateCallers.add(caller);
//        System.out.println("Added Element: " + caller);
    }
    public void initSootEnv() {
        G.reset();
        Set<String> realPath = new HashSet<>();
        for (String clsPath : config.classPath) {
            if (clsPath.contains("!")) {
                String jarPath = clsPath.substring(0, clsPath.indexOf("!"));
                String appPath = clsPath.substring(clsPath.lastIndexOf("!") + 1);
                String uuid = UUID.nameUUIDFromBytes(clsPath.getBytes()).toString().substring(0, 7);
                String newPath = "src/main/resources/" + uuid;
                if (!new File(newPath).exists())
                    ClassNameExtractor.processJarFile(jarPath, newPath, appPath);
                realPath.add(newPath);
            } else {
                realPath.add(clsPath);
            }
        }

        String Separator = File.pathSeparator;
        String sootCp = "src/main/resources/rt.jar" + Separator + String.join(Separator, realPath);
        System.out.println("classpath: " + sootCp);
        Options.v().set_soot_classpath(sootCp);
        // 这条语句要谨慎，加入后会全部分析
        Options.v().set_process_dir(realPath.stream().toList());

        // 设置 Soot 选项
        soot.options.Options.v().set_app(true);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        soot.options.Options.v().set_exclude(excludeClasses());
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_verbose(true);

        // 禁用 all-reachable 选项
        Options.v().setPhaseOption("cg", "all-reachable:false");
        // 选择调用图算法，例如 RTA
        Options.v().setPhaseOption("cg", "rta:true");
        Options.v().setPhaseOption("cg.cha", "on:false");
        Options.v().setPhaseOption("cg.spark", "on:false");
        Options.v().setPhaseOption("jb", "use-original-names:true");

        Options.v().set_output_format(Options.output_format_none);

        // 加载入口类
        entryClass = Scene.v().loadClassAndSupport(config.source.className);
        entryMethod =  entryClass.getMethod(config.source.method);
        if (config.source.method.equals("void main(java.lang.String[])")) {
            Scene.v().setMainClass(entryClass);
        }
        entryPoints.add(entryMethod);

        // 加载必要的类
        Scene.v().loadNecessaryClasses();
        autoWired.scanAppClasses();
        // 设置入口点
        System.out.println("Entry points: " + entryPoints);
        Scene.v().setEntryPoints(new ArrayList<>(entryPoints));
        // 运行 Soot，生成ActiveBody
        PackManager.v().runPacks();

        System.out.println("Classes size: " + Scene.v().getClasses().size());
        System.out.println("Application classes size: " + Scene.v().getApplicationClasses().size());

    }

    public void addEntryPoint(SootMethod method) {
        entryPoints.add(method);
    }
    private List<String> excludeClasses() {
        return Arrays.asList("java.*", "javax.*", "sun.*", "jdk.*", "com.sun.*", "com.fasterxml.*",
                "org.eclipse.*", "org.glassfish.*", "javassist.*",
                "org.springframework.*");
    }
}
