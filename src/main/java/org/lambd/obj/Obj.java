package org.lambd.obj;

import org.lambd.SpMethod;
import org.lambd.condition.Condition;
import org.lambd.pointer.InstanceField;
import org.lambd.pointer.Pointer;
import org.lambd.transformer.SpStmt;
import org.lambd.transition.RetTrans;
import soot.RefType;
import soot.SootField;
import soot.Type;
import soot.jimple.Stmt;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Obj 代表一个抽象的Java对象，例如：o = New Object()
 */
public abstract class Obj {
    protected final Type type;
    private final SpStmt stmt;
    protected Map<SootField, InstanceField> fieldPointer = new HashMap<>();
    public Obj(Type type, SpStmt stmt) {
        this.type = type;
        this.stmt = stmt;
    }
    public Type getType() {
        return type;
    }
    public SpMethod getContainer() {
        return stmt.getContainer();
    }
    public SpStmt getStmt() {
        return stmt;
    }
    public Condition getCondition() {
        return stmt.getCondition();
    }
    public abstract Obj castClone(SpStmt stmt, @Nullable Type newType);
    public String toString() {
        return String.format("Obj: [type:%s, born: %s]", type, stmt.toString());
    }
    public InstanceField fieldPointer(SootField field) {
        return fieldPointer.computeIfAbsent(field, f -> new InstanceField(this, field));
    }
}
