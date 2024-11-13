package org.lambd.obj;

import org.lambd.SpMethod;
import org.lambd.transition.RetTrans;
import soot.RefType;
import soot.Type;
import soot.jimple.Stmt;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Obj 代表一个抽象的Java对象，例如：o = New Object()
 */
public interface Obj {
    Type getType();
    Obj castClone(Stmt stmt, @Nullable Type newType);
    default boolean isMayMultiple() {
        return false;
    }
    default boolean isInterface() {
        return getType() instanceof RefType rt && rt.getSootClass().isInterface();
    }
}
