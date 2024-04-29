package org.lambd.obj;

import soot.SootField;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class GenObj extends FormatObj {
    protected final String field;
    private FormatObj parent;

    public GenObj(FormatObj parent, String field) {
        super(parent.type, parent.container, parent.getIndex());
        this.field = field;
        this.parent = parent;
        this.fields = parent.fields.isEmpty() ? field : parent.fields.concat("." + field);
    }
    public String getField() {
        return field;
    }
    public FormatObj getParent() {
        return parent;
    }
}
