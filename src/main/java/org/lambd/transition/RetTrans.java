package org.lambd.transition;

import org.lambd.SpMethod;
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
    public void apply(SpMethod method, Stmt stmt) {
        method.handleReturn(stmt, type);
    }
    public static void main(String[] args) {
        Set<RetTrans> retSet = new HashSet<>();
        RetTrans r1 = new RetTrans(RefType.v("java.lang.String"));
        RetTrans r2 = new RetTrans(RefType.v("java.lang.String"));
        retSet.add(r1);
        retSet.add(r2);
        System.out.println(retSet.size());
    }
}
