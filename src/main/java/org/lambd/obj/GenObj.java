package org.lambd.obj;

import soot.RefType;
import soot.SootField;
import soot.Type;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class GenObj extends FormatObj {
    protected final String field;
    private FormatObj parent;

    public GenObj(FormatObj parent, String field, Type type) {
        super(type, parent.container, parent.getIndex());
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
