package org.lambd.wrapper;

import org.lambd.SootWorld;
import org.lambd.annotation.Annotation;
import org.lambd.pointer.StaticField;
import soot.*;
import soot.jimple.InvokeStmt;
import soot.jimple.StaticInvokeExpr;

import java.util.*;

public class SpClass implements Wrapper {
    enum State {
        UNSCANNED,
        SCANNED,
        VISITED,
        FINISHED,
    }
    private SootClass sc;
    private State state;
    private List<Annotation> annotionList = new ArrayList<>();
    private SootMethod clinitMethod = null;
    public static final String CLINIT = "void <clinit>()";
    private Map<SootField, StaticField> statics = new HashMap<>();
    private Map<SootField, Set<Type>> RTAFields = new HashMap<>();
    public SpClass(SootClass sc) {
        this.sc = sc;
        state = State.UNSCANNED;
    }
    public void scan() {
        if (state == State.UNSCANNED)
            state = State.SCANNED;
    }
    public void visit() {
        if (state == State.VISITED)
            return;
        state = State.VISITED;
        SootWorld sw = SootWorld.v();
        if (sc.hasSuperclass()) {
            sw.getClass(sc.getSuperclass()).visit();
        }
        if (clinitMethod != null)
            sw.visitMethod(sw.getMethod(clinitMethod), method -> {}, true);
    }
    public void setClinitMethod(SootMethod sm) {
        clinitMethod = sm;
    }
    public void addFieldType(SootField field, Type type) {
        RTAFields.computeIfAbsent(field, f -> new HashSet<>()).add(type);
    }
    public Set<Type> getFieldTypes(SootField field) {
        return RTAFields.get(field);
    }
    public SootClass getSootClass() {
        return sc;
    }
    public void addAnnotation(Annotation an) {
        annotionList.add(an);
    }
    public StaticField getStaticField(SootField field) {
        return statics.computeIfAbsent(field,
                f -> new StaticField(field));
    }
}

