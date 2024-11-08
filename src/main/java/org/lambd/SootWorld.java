package org.lambd;

import org.lambd.transition.*;
import org.lambd.utils.ClassNameExtractor;
import soot.*;
import soot.jimple.*;
import soot.options.Options;
import soot.tagkit.AnnotationStringElem;
import soot.tagkit.AnnotationTag;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;

import java.io.IOException;
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
    private List<SootClass> shellComponents = new ArrayList<>();
    private Set<SpMethod> updateCallers = new HashSet<>();
    private NeoGraph graph;
    private SootWorld() {
        graph = new NeoGraph("bolt://localhost:7687", "neo4j", "123456", false);
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
    public SootMethod getEntryMethod() {
        return entryMethod;
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
    private Map<SootMethod, Integer> calleeMap = new HashMap<>();
    private int calleeID = 0;
    public int getCalleeID(SootMethod callee) {
        return calleeMap.computeIfAbsent(callee, k -> calleeID ++);
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
    public void visitMethod(SootMethod method) {
        visitMethod(method, false);
    }
    public void visitMethod(SootMethod method, boolean must) {
        if (!must && (!method.getDeclaringClass().isApplicationClass() || visited.contains(method)))
            return;
        visited.add(method);
        SpMethod spMethod = getMethod(method);
        StmtVisitor visitor = new StmtVisitor(spMethod);
        for (Unit unit : method.getActiveBody().getUnits()) {
            visitor.visit((Stmt) unit);
        }
        spMethod.getSummary().print(true);
        spMethod.finish();
        graph.updateMethodSummary(spMethod);
    }
    public void updateNeo4jRelation(SootMethod caller, SootMethod callee) {
        graph.createRelationWithMethods(caller, callee);
    }
    public SpMethod getMethod(SootMethod method) {
        return methodMap.computeIfAbsent(method, k -> new SpMethod(method, getCalleeID(method)));
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
    public void analyze(SootMethod entry) {
        visitMethod(entry);
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
    public void checkMethodAnnotation(List<SootClass> classes) {
        for (SootClass sc: classes) {
            for (SootMethod sm : sc.getMethods()) {

                VisibilityAnnotationTag tag = (VisibilityAnnotationTag) sm.getTag("VisibilityAnnotationTag");
                SpMethod spMethod = getMethod(sm);
                if (tag != null) {
                    tag.getAnnotations().forEach(anno -> {
                        String type = anno.getType();
                        Map<String, String> elements = new HashMap<>();
                        anno.getElems().forEach(e -> {
                            String name = e.getName();
                            if (e instanceof AnnotationStringElem se) {
                                elements.put(name, se.getValue());
                            }
                        });
                        Annotion annotion = new Annotion(type, elements);
                        spMethod.addAnnotation(annotion);
                    });
                }
                if (spMethod.checkAnnotation()) {
                    System.out.printf("entry method: %s, since %s annotation\n", sm.getName(), spMethod.getAnnotation());
                    visitMethod(sm);
                }
            }
        }
    }
    public void analyzePackage(Set<String> packageName) {
        List<SootClass> classes = new ArrayList<>();
        packageName.forEach(pkg -> {
            Scene.v().getApplicationClasses().forEach(sc -> {
                if (sc.getPackageName().startsWith(pkg))
                    classes.add(sc);

            });
        });
        //        Scene.v().loadNecessaryClasses();
        checkMethodAnnotation(classes);
    }
    public void checkShellComponents() {
        List<SootClass> classes = new ArrayList<>();
        for (SootClass sootClass: Scene.v().getApplicationClasses()) {
            // 获取类的所有标签
            List<Tag> tags = sootClass.getTags();
            for (Tag tag : tags) {
                // 检查是否为可见性注解标签
                if (tag instanceof VisibilityAnnotationTag) {
                    VisibilityAnnotationTag visibilityTag = (VisibilityAnnotationTag) tag;
                    // 获取所有注解
                    List<AnnotationTag> annotations = visibilityTag.getAnnotations();

                    // 遍历注解
                    for (AnnotationTag annotation : annotations) {
                        // 获取注解的类型，格式为 L包名/类名;
                        String annotationType = annotation.getType();

                        // 检查是否为 @ShellComponent 注解
                        if (annotationType.equals("Lorg/springframework/shell/standard/ShellComponent;")) {
                            classes.add(sootClass);
                        }
                    }
                }
            }
        }
        checkMethodAnnotation(classes);
    }
    public void addCaller(SpMethod caller) {
        updateCallers.add(caller);
//        System.out.println("Added Element: " + caller);
    }
    public void initSootEnv() {
        G.reset();
        List<String> realPath = new ArrayList<>();
        for (String clsPath : config.classPath) {
            if (clsPath.contains("!")) {
                try {
                    realPath.add( ClassNameExtractor.extractBootInfClasses(clsPath));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                realPath.add(clsPath);
            }
        }

        String sootCp = "src/main/resources/rt.jar;" + String.join(";", realPath);
        System.out.println("classpath: " + sootCp);
        Options.v().set_soot_classpath(sootCp);
        // 这条语句要谨慎，加入后会全部分析
        Options.v().set_process_dir(realPath);

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

        Options.v().set_output_format(Options.output_format_none);

        // 加载入口类
        entryClass = Scene.v().loadClassAndSupport(config.source.className);
        entryMethod =  entryClass.getMethod(config.source.method);
        if (config.source.method.equals("void main(java.lang.String[])")) {
            Scene.v().setMainClass(entryClass);
        }
        List<SootMethod> entryPoints = new ArrayList<>();
        entryPoints.add(entryMethod);
        // 设置入口点
        Scene.v().setEntryPoints(entryPoints);

        // 加载必要的类
        Scene.v().loadNecessaryClasses();

        // 运行 Soot
        PackManager.v().runPacks();

        System.out.println("Classes size: " + Scene.v().getClasses().size());
        System.out.println("Classes size: " + Scene.v().getApplicationClasses().size());
        checkShellComponents();
    }

    private List<String> excludeClasses() {
        return Arrays.asList("java.*", "javax.*", "sun.*", "jdk.*", "com.sun.*", "com.fasterxml.*",
                "org.eclipse.*", "org.glassfish.*", "javassist.*",
                "org.springframework.*");
    }
}
