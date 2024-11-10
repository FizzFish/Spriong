package org.lambd.anonotation;

import org.lambd.SootWorld;
import org.lambd.SpMethod;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoWired {
    private Map<RefType, SootClass> autowrireds = new HashMap<>();
    public void addBean(RefType type, SootClass cls)
    {
        autowrireds.put(type, cls);
    }
    public SootClass getBean(RefType type)
    {
        return autowrireds.get(type);
    }
    public void wired(SootClass sootClass)
    {
        List<SootMethod> constructors = new ArrayList<>();
        for (SootMethod method : sootClass.getMethods()) {
            if (method.isConstructor()) {
                constructors.add(method);
            }
        }
        if (constructors.size() == 1) {
            SpMethod initMethod = SootWorld.v().getMethod(constructors.get(0));
            Annotation annotation = new Annotation(AnnotationType.AUTOWIRED, null);
            initMethod.addAnnotation(annotation);
        }
    }
}
