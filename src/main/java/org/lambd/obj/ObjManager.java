package org.lambd.obj;

import org.lambd.transformer.SpStmt;
import org.lambd.transition.Weight;
import soot.Local;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.jimple.Stmt;

public interface ObjManager {
    void copy(Local from, Local to);
    // x = y.f
    void loadField(Local to, Local base, SootField field);
    // x = C.f
    void loadStaticField(Local to, Class clazz, SootField field);
    // x.f = y
    void storeField(Local base, SootField field, Local from, SpStmt stmt);
    // C.f = y
    void storeStaticField(Class clazz, SootField field, Local from);
    // x = y[i]
    void loadArray(Local to, Local base, SpStmt stmt);
    // x[i] = y
    void storeArray(Local base, Local from, SpStmt stmt);
}
