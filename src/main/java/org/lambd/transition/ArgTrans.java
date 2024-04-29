package org.lambd.transition;

import org.lambd.SpMethod;
import org.lambd.utils.PrimeGenerator;
import org.lambd.utils.Utils;
import soot.jimple.Stmt;

import java.util.Objects;

public record ArgTrans(int from, int to, Weight w) implements Transition {
    @Override
    public void apply(SpMethod method, Stmt stmt) {
        method.handleTransition(stmt, from, to, w);
    }

    public String toString() {
        return String.format("%s.%s = %s", Utils.argString(to), w, Utils.argString(from));
    }
    public int hashCode() {
        return Objects.hash(from, to);
    }
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ArgTrans) {
            ArgTrans other = (ArgTrans) obj;
            return from == other.from && to == other.to;
        }
        return false;
    }

}