package org.lambd.obj;

import org.lambd.SootWorld;
import org.lambd.pointer.InstanceField;
import org.lambd.transformer.SpStmt;
import org.lambd.wrapper.SpClass;
import soot.SootField;
import soot.Type;

import javax.annotation.Nullable;

public class SourceObj extends RealObj {
    public SourceObj(Type type, SpStmt stmt) {
        super(type, stmt);
    }
    @Override
    public boolean real() {
        return false;
    }
    public InstanceField fieldPointer(SootField field) {
        SootWorld sw = SootWorld.v();

        InstanceField instanceField = fieldPointer.computeIfAbsent(field, f -> new InstanceField(this, field));
        if (field.getName().equals("[*]")) {
            instanceField.add(new RealObj(field.getType(), stmt));
        } else {
            SpClass sc = sw.getClass(field.getDeclaringClass());
            sc.getFieldTypes(field).forEach(t -> instanceField.add(new SourceObj(t, stmt)));
        }
        return instanceField;
    }
    @Override
    public Obj castClone(SpStmt stmt, @Nullable Type newType) {
        if (newType == null || newType.equals(type))
            return this;
        return new SourceObj(newType, stmt);
    }
}
