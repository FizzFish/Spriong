package org.lambd.obj;

import soot.RefType;
import soot.SootField;
import soot.Type;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class GenObj extends FormatObj {
    protected final SootField field;
    private FormatObj parent;

    public GenObj(FormatObj parent, SootField field, Type type) {
        super(type, parent.stmt, parent.getIndex());
        this.field = field;
        this.parent = parent;
        this.fields.addAll(parent.fields);
        this.fields.add(field);
    }
    public String toString() {
        return String.format("FormatObj: %d.%s", getIndex(), getFields());
    }

}
