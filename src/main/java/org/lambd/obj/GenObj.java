package org.lambd.obj;

import soot.RefType;
import soot.SootField;
import soot.Type;
import soot.jimple.Stmt;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * GeObj代表参数对象的fields对应的对象，尽管在函数内部我们不知道arg.fields是否对应真是的对象，但是我们会假设这些对象存在
 */
public class GenObj extends FormatObj {
    protected final SootField field;
    private FormatObj parent;

    public GenObj(FormatObj parent, SootField field, Type type, Stmt stmt) {
        super(type, stmt, parent.getIndex());
        this.field = field;
        this.parent = parent;
        this.fields.addAll(parent.fields);
        this.fields.add(field);
    }
    public String toString() {
        return String.format("FormatObj: %d.%s", getIndex(), getFields());
    }

}
