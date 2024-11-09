package org.lambd;

import org.lambd.anonotation.Annotation;
import soot.*;

import java.util.ArrayList;
import java.util.List;

public class SpSootClass {
    private SootClass sc;
    private List<Annotation> annotionList = new ArrayList<>();
    public SpSootClass(SootClass sc) {
        this.sc = sc;
    }
    public void addAnnotation(Annotation an) {
        annotionList.add(an);
    }
}
