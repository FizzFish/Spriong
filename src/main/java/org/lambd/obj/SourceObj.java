package org.lambd.obj;

import org.lambd.SootWorld;
import org.lambd.pointer.InstanceField;
import org.lambd.transformer.SpStmt;
import org.lambd.wrapper.SpClass;
import soot.SootField;
import soot.Type;

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
        SpClass sc =  sw.getClass(field.getDeclaringClass());
        InstanceField instanceField = fieldPointer.computeIfAbsent(field, f -> new InstanceField(this, field));
        sc.getFieldTypes(field).forEach(t -> instanceField.add(new SourceObj(t, stmt)));
        return instanceField;
    }
}
