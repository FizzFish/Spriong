package org.lambd.obj;

import org.apache.commons.math3.fraction.Fraction;
import org.lambd.SpMethod;
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

    public OneObjManager(SpMethod method)
    {
        this.method = method;
    }

    @Override
    public void copy(Local from, Local to) {
        method.copy(from, to, Weight.ONE);
    }
    public void loadField(Local to, Local base, SootField field) {
        // x = y.f -> w(y,x) = f
        Weight w = new Weight(field.getName(), 1);
        method.copy(base, to, w);
    }
    public void loadStaticField(Local to, Class clazz, SootField field) {
        // x = C.f

    }

    public void storeField(Local base, SootField field, Local from) {
        // x.f = y -> w(y,x) = 1/f
        Weight w = new Weight(field.getName(), -1);
        w.setUpdate(Weight.EFFECT); // we should keep store field stmt
        method.copy(from, base, w);
    }
    public void storeStaticField(Class clazz, SootField field, Local from) {
        // C.f = y

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
