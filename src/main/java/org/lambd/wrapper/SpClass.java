package org.lambd.wrapper;

import org.lambd.SootWorld;
import org.lambd.annotation.Annotation;
import org.lambd.pointer.StaticField;
import soot.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpClass implements Wrapper {
    private SootClass sc;
    private State state;
    private List<Annotation> annotionList = new ArrayList<>();
    private SootMethod clinitMethod = null;
    public static final String CLINIT = "void <clinit>()";
    private Map<SootField, StaticField> statics = new HashMap<>();
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
enum State {
    UNSCANNED,
    SCANNED,
    VISITED,
    FINISHED,
}
