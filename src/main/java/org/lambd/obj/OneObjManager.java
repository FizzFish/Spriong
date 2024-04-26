package org.lambd.obj;

import org.apache.commons.math3.fraction.Fraction;
import org.lambd.SpMethod;
import org.lambd.pointer.InstanceField;
import org.lambd.pointer.PointerToSet;
import org.lambd.pointer.StaticField;
import org.lambd.pointer.VarPointer;
import org.lambd.transition.Weight;
import org.lambd.utils.PrimeGenerator;
import org.lambd.utils.Utils;
import soot.Local;
import soot.SootField;
import soot.SootMethod;

import java.util.HashMap;
import java.util.Map;

public class OneObjManager implements ObjManager {
    private final SpMethod method;
    private PointerToSet ptset;

    public OneObjManager(SpMethod method)
    {
        this.method = method;
        ptset = new PointerToSet(method);
    }

    @Override
    public void copy(Local from, Local to) {
        VarPointer fromPointer = ptset.getVarPointer(from);
        VarPointer toPointer = ptset.getVarPointer(to);
        ptset.copy(fromPointer, toPointer);
    }
    public void loadField(Local to, Local base, SootField field) {
        // x = y.f -> w(y,x) = f
        ptset.getLocalObjs(base).forEach(obj -> {
            InstanceField fromPointer = ptset.getInstanceField(obj, field);
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

    public void storeField(Local base, SootField field, Local from) {
        // x.f = y -> w(y,x) = 1/f
        // analysis alias
        ptset.getLocalObjs(base).forEach(obj -> {
            VarPointer fromPointer = ptset.getVarPointer(from);
            InstanceField toPointer = ptset.getInstanceField(obj, field);
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
        Weight w = new Weight(Utils.arrayStr, 1);
        method.copy(base, to, w);
    }
    public void storeArray(Local base, Local from) {
        // x[i] = y
        Weight w = new Weight(Utils.arrayStr, -1);
        w.setUpdate(Weight.EFFECT);
        method.copy(from, base, w);
    }

}
