package org.lambd.obj;

import org.lambd.SpMethod;
import org.lambd.pointer.*;
import org.lambd.transition.Weight;
import org.lambd.utils.Utils;
import soot.Local;
import soot.SootField;
import soot.Type;
import soot.jimple.Stmt;

public class OneObjManager implements ObjManager {
    private final SpMethod container;
    private PointerToSet ptset;

    public OneObjManager(SpMethod container, PointerToSet ptset)
    {
        this.container = container;
        this.ptset = ptset;
    }

    @Override
    public void copy(Local from, Local to) {
        // to = from
        VarPointer fromPointer = ptset.getVarPointer(from);
        if (from.getName().startsWith("$") && to.getName().equals("workingBuilder")) // workingBuilder = $stack51;
            // merge to with from
            ptset.getSameVP(from, to);
        else {
            VarPointer toPointer = ptset.getVarPointer(to);
            ptset.copy(fromPointer, toPointer);
        }
    }
    public void loadField(Local to, Local base, SootField field) {
        // x = y.f
        ptset.getLocalObjs(base).forEach(obj -> {
            InstanceField fromPointer = ptset.getInstanceField(obj, field.getName());
            VarPointer toPointer = ptset.getVarPointer(to);
            ptset.copy(fromPointer, toPointer);
        });
    }

    public void loadStaticField(Local to, Class clazz, SootField field) {
        // x = C.f
        StaticField fromPointer = ptset.getStaticField(field);
        VarPointer toPointer = ptset.getVarPointer(to);
        ptset.copy(fromPointer, toPointer);
    }

    public void storeField(Local base, SootField field, Local from, Stmt stmt) {
        // x.f = y -> w(y,x) = 1/f
        // analysis alias
        VarPointer fromPointer = ptset.getVarPointer(from);
        ptset.getLocalObjs(base).forEach(obj -> {
            InstanceField toPointer = ptset.getInstanceField(obj, field.getName());
            if (obj instanceof FormatObj formatObj)
                ptset.storeAlias(fromPointer, formatObj, field.getName(), stmt);
            ptset.copy(fromPointer, toPointer);
        });
    }
    public void storeStaticField(Class clazz, SootField field, Local from) {
        // C.f = y
        VarPointer fromPointer = ptset.getVarPointer(from);
        StaticField toPointer = ptset.getStaticField(field);
        ptset.copy(fromPointer, toPointer);
    }
    public void loadArray(Local to, Local base) {
        // x = y[i]
        ptset.getLocalObjs(base).forEach(obj -> {
//            assert obj instanceof ArrayObj;
            InstanceField fromPointer = ptset.getInstanceField(obj, Utils.arrayStr);
            VarPointer toPointer = ptset.getVarPointer(to);
            ptset.copy(fromPointer, toPointer);
        });
    }
    public void storeArray(Local base, Local from, Stmt stmt) {
        // x[i] = y
        ptset.getLocalObjs(base).forEach(obj -> {
            VarPointer fromPointer = ptset.getVarPointer(from);
            if (obj instanceof FormatObj formatObj)
                ptset.storeAlias(fromPointer, formatObj, Utils.arrayStr, stmt);
            InstanceField toPointer = ptset.getInstanceField(obj, Utils.arrayStr);
            ptset.copy(fromPointer, toPointer);
        });
    }
    public void handleNew(Local var, Type type) {
        Obj obj = new NewObj(type, container);
        ptset.addLocal(var, obj);
    }

}
