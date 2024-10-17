package org.lambd.transition;

import org.lambd.SpMethod;
import org.lambd.utils.PrimeGenerator;
import org.lambd.utils.Utils;
import soot.jimple.Stmt;

import java.util.Objects;

/**
 * 与BaseTrans类似，其实可以合并
 */
public class ArgTrans implements Transition {
    private int from, to;
    private Weight weight;
    private Stmt born;
    public ArgTrans(int from, int to, Weight weight, Stmt born) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.born = born;
    }
    @Override
    public void apply(SpMethod method, Stmt stmt) {
        method.handleTransition(stmt, from, to, weight);
    }

    public String toString() {
        return String.format("%s.%s = %s.%s", Utils.argString(to), Utils.fieldString(weight.getToFields()),
                Utils.argString(from), Utils.fieldString(weight.getFromFields()));
    }
    public int hashCode() {
        return Objects.hash(from, to);
    }
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ArgTrans at) {
            return weight.equals(at.weight);
        }
        return false;
    }

}