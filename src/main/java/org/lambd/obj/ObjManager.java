package org.lambd.obj;

import soot.Local;
import soot.SootField;
import soot.SootMethod;
import soot.Value;

public interface ObjManager {
    void copy(Local from, Local to);
    void copy(Local from, Local to, Relation relation);
    void addObj(Local value, Location obj);
    void show();
    // x = y.f
    void loadField(Local to, Local base, SootField field);
    // x = C.f
    void loadStaticField(Local to, Class clazz, SootField field);
    // x.f = y
    void storeField(Local base, SootField field, Local from);
    // C.f = y
    void storeStaticField(Class clazz, SootField field, Local from);
    // x = y[i]
    void loadArray(Local to, Local base);
    // x[i] = y
    void storeArray(Local base, Local from);
    void invoke(Local to, Local base, SootMethod method);
}
