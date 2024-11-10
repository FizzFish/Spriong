package org.lambd.anonotation;

import org.lambd.SootWorld;
import org.lambd.SpMethod;
import org.lambd.wrapper.SpSootClass;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.tagkit.AnnotationTag;
import soot.tagkit.Tag;
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
                if (Annotation.hasAnnotation(method, AnnotationType.AUTOWIRED))
                    SootWorld.v().addEntryPoint(method);
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
    public void scanSpring() {
//        System.out.println(Scene.v().getApplicationClasses());
        for (SootClass sootClass: Scene.v().getApplicationClasses()) {
            SpSootClass spSootClass = SootWorld.v().getClass(sootClass);
            List<Tag> tags = sootClass.getTags();
            for (Tag tag : tags) {
                if (tag instanceof VisibilityAnnotationTag visibilityTag) {
                    List<AnnotationTag> annotations = visibilityTag.getAnnotations();

                    // 遍历注解
                    for (AnnotationTag anno : annotations) {
                        Annotation annotation = Annotation.extractAnnotation(anno);
                        if (annotation != null) {
                            spSootClass.addAnnotation(annotation);
                            annotation.apply(spSootClass);
                        }
                    }
                }
            }
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
