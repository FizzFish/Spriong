package org.lambd.wrapper;

import org.lambd.anonotation.Annotation;
import soot.*;

import java.util.ArrayList;
import java.util.List;

public class SpSootClass implements Wrapper {
    private SootClass sc;
    private List<Annotation> annotionList = new ArrayList<>();
    public SpSootClass(SootClass sc) {
        this.sc = sc;
    }
    public SootClass getSootClass() {
        return sc;
    }
    public void addAnnotation(Annotation an) {
        annotionList.add(an);
    }
}
