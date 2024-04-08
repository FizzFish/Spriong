package org.lambd;

import soot.Local;
import soot.SootField;

import java.util.List;

public class AccessPath {
    private final Local base;
    private List<SootField> fields;
    public AccessPath(Local base, List<SootField> fields) {
        this.base = base;
        this.fields = fields;
    }
}
