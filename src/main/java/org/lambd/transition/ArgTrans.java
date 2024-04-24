package org.lambd.transition;

import org.lambd.SpMethod;
import org.lambd.utils.PrimeGenerator;
import org.lambd.utils.Utils;
import soot.jimple.Stmt;

public record ArgTrans(int from, int to, Weight w) implements Transition {
    @Override
    public void apply(SpMethod method, Stmt stmt) {
        method.handleTransition(stmt, from, to, w);
    }

    public String toString() {
        return String.format("%s.%s = %s.%s", Utils.argString(to), w.getPositive(), Utils.argString(from), w.getNegative());
    }

}