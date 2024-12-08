package org.lambd.annotation;

import org.lambd.SootWorld;
import org.lambd.SpMethod;
import org.lambd.transition.SinkTrans;
import org.lambd.transition.Weight;
import org.lambd.wrapper.SpClass;
import org.lambd.wrapper.Wrapper;
import soot.*;
import soot.tagkit.VisibilityAnnotationTag;

import java.util.*;

public class AutoWired {
    // autowired beans
    private Map<RefType, SpClass> autowrireds = new HashMap<>();
    // grpc services
    private Map<SootClass, List<String>> services = new HashMap<>();
    // replace with actual source feature
    private List<String> entryPoints = List.of("source");
    private Map<String, Integer> sinks = Map.of("send",0);
    public void addBean(RefType type, SpClass cls)
    {
        autowrireds.put(type, cls);
    }
    public SpClass getBean(RefType type)
    {
        return autowrireds.get(type);
    }
    public void wired(SpClass ssc)
    {
        List<SootMethod> constructors = new ArrayList<>();
        for (SootMethod method : ssc.getSootClass().getMethods()) {
            if (method.isConstructor()) {
                constructors.add(method);
                /**
                if (Annotation.hasAnnotation(method, AnnotationType.AUTOWIRED))
                    SootWorld.v().addEntryPoint(method);
                 */
            }
        }
        if (constructors.size() == 1) {
            SootWorld.v().addEntryPoint(constructors.get(0));
        }
    }
    public void addService(SootClass sc, String service)
    {
        services.computeIfAbsent(sc, k -> new ArrayList<>()).add(service);
    }
    public void scanShellMethod(SpClass ssc) {
        for (SootMethod method : ssc.getSootClass().getMethods()) {
            if (Annotation.hasAnnotation(method, AnnotationType.SHELLMETHOD)) {
                SootWorld.v().addEntryPoint(method);
            }
        }
    }
    public void scanAppClasses() {
        SootWorld sw = SootWorld.v();
        for (SootClass sc: Scene.v().getApplicationClasses()) {
            analyzeAnnotation(sc);
            for (SootMethod sm: sc.getMethods()) {
                analyzeAnnotation(sm);
                String name = sm.getName();
                if (entryPoints.contains(name))
                    SootWorld.v().addEntryPoint(sm);
                if (sinks.containsKey(name)) {
                    // <javax.script.ScriptEngine: java.lang.Object eval(java.lang.String)>
                    String signature = String.format("<%s: %s>", sc.getName(), sm.getSignature());
                    SootWorld.v().addTransition(signature, new SinkTrans(signature, sinks.get(name), Weight.ONE));
                }
                if (sm.getSubSignature().equals(SpClass.CLINIT)) {
                    SpClass spc = sw.getClass(sc);
                    spc.setClinitMethod(sm);
                }
            }
            sc.getMethods().forEach(this::analyzeAnnotation);
        }
        for (SootClass sc: Scene.v().getApplicationClasses())
            checkInterface(sc);
    }
    private void checkInterface(SootClass sc) {
        String bindService = "io.grpc.BindableService";
        // check if contains grpc interface
        if (sc.getInterfaces().stream().map(SootClass::getName).noneMatch(bindService::equals))
            return;
        if (sc.isInterface())
            return;
        SootClass outerClass = sc.getOuterClass();
        List<String> serivceMethods = services.get(outerClass);

        FastHierarchy fastHierarchy = Scene.v().getFastHierarchy();
        SootWorld sw = SootWorld.v();
        for (SootClass subClass: fastHierarchy.getSubclassesOf(sc)) {
            for (SootMethod sm: subClass.getMethods()) {
                // add grpc entry
                if (serivceMethods.contains(sm.getName()))
                    sw.addEntryPoint(sm);
            }
        }
    }
    private void analyzeAnnotation(Object obj) {
        VisibilityAnnotationTag tag = null;
        SootWorld sw = SootWorld.v();
        boolean classOrMethod = obj instanceof SootClass;
        if (classOrMethod) {
            tag = (VisibilityAnnotationTag) ((SootClass) obj).getTag("VisibilityAnnotationTag");
            SpClass ssc = sw.getClass((SootClass) obj);
            ssc.scan();
        } else
            tag = (VisibilityAnnotationTag) ((SootMethod) obj).getTag("VisibilityAnnotationTag");
        if (tag != null) {
            tag.getAnnotations().forEach(anno -> {
                Annotation annotation = Annotation.extractAnnotation(anno);
                if (annotation != null) {
                    Wrapper wrapper = classOrMethod ? sw.getClass((SootClass) obj) : sw.getMethod((SootMethod) obj);
                    wrapper.addAnnotation(annotation);
                    annotation.apply(wrapper);
                }
            });
        }
    }
    public void scanMethodInClass(SootClass sc) {
        for (SootMethod sm : sc.getMethods()) {

            VisibilityAnnotationTag tag = (VisibilityAnnotationTag) sm.getTag("VisibilityAnnotationTag");
            SpMethod spMethod = SootWorld.v().getMethod(sm);
            if (tag != null) {
                tag.getAnnotations().forEach(anno -> {
                    Annotation annotation = Annotation.extractAnnotation(anno);
                    if (annotation != null) {
                        spMethod.addAnnotation(annotation);
                        annotation.apply(spMethod);
                    }
                });
            }
        }
    }
}
