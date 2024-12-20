package org.lambd.transition;

import org.lambd.SpMethod;
import org.lambd.transformer.SpStmt;
import soot.RefType;
import soot.jimple.Stmt;

import java.util.HashSet;
import java.util.Set;

public class RetTrans implements Transition {
    private RefType type;

    public RetTrans(RefType type) {
        this.type = type;
    }
    public boolean equals(Object obj) {
        if (obj instanceof RetTrans st)
            return type.equals(st.type);
        return false;
    }
    public int hashCode() {
        return type.hashCode();
    }
    public void apply(SpMethod caller, SpStmt stmt) {
        caller.handleReturn(stmt, type);
    }
}
