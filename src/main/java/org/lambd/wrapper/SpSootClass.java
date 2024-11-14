package org.lambd.wrapper;

import org.lambd.annotation.Annotation;
import soot.*;

import java.util.ArrayList;
import java.util.List;

public class SpSootClass implements Wrapper {
    private SootClass sc;
    private State state;
    private List<Annotation> annotionList = new ArrayList<>();
    public SpSootClass(SootClass sc) {
        this.sc = sc;
        state = State.UNSCANNED;
    }
    public void scan() {
        if (state == State.UNSCANNED)
            state = State.SCANNED;
    }
    public SootClass getSootClass() {
        return sc;
    }
    public void addAnnotation(Annotation an) {
        annotionList.add(an);
    }
}
enum State {
    UNSCANNED,
    SCANNED,
    FINISHED,
}
