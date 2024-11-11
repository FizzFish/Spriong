package org.lambd.anonotation;

import org.lambd.SootWorld;
import org.lambd.SpMethod;
import org.lambd.wrapper.SpSootClass;
import org.lambd.wrapper.Wrapper;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.tagkit.VisibilityAnnotationTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoWired {
    private Map<RefType, SpSootClass> autowrireds = new HashMap<>();
    public void addBean(RefType type, SpSootClass cls)
    {
        autowrireds.put(type, cls);
    }
    public SpSootClass getBean(RefType type)
    {
        return autowrireds.get(type);
    }
    public void wired(SpSootClass ssc)
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
    public void scanShellMethod(SpSootClass ssc) {
        for (SootMethod method : ssc.getSootClass().getMethods()) {
            if (Annotation.hasAnnotation(method, AnnotationType.SHELLMETHOD)) {
                SootWorld.v().addEntryPoint(method);
            }
        }
    }
    public void scanAppClasses() {
        for (SootClass sc: Scene.v().getApplicationClasses()) {
            analyzeAnnotation(sc);
            sc.getMethods().forEach(this::analyzeAnnotation);
        }
    }
    private void analyzeAnnotation(Object obj) {
        VisibilityAnnotationTag tag = null;
        SootWorld sw = SootWorld.v();
        boolean classOrMethod = obj instanceof SootClass;
        if (classOrMethod)
            tag = (VisibilityAnnotationTag) ((SootClass) obj).getTag("VisibilityAnnotationTag");
        else
            tag = (VisibilityAnnotationTag) ((SootMethod) obj).getTag("VisibilityAnnotationTag");
        Wrapper wrapper = classOrMethod ? sw.getClass((SootClass) obj) : sw.getMethod((SootMethod) obj);
        if (tag != null) {
            tag.getAnnotations().forEach(anno -> {
                Annotation annotation = Annotation.extractAnnotation(anno);
                if (annotation != null) {
//                    Wrapper wrapper = classOrMethod ? sw.getClass((SootClass) obj) : sw.getMethod((SootMethod) obj);
                    wrapper.addAnnotation(annotation);
                    System.out.println("Annotation: " + annotation);
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
