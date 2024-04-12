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

    @Override
    public boolean isReturnTrans() {
        return to == -2;
    }

    public String toString() {
        int numerator = w.getFraction().getNumerator();
        int denominator = w.getFraction().getDenominator();
        String s1 = numerator == 1? "" : PrimeGenerator.v().express(numerator);
        String s2 = denominator == 1? "" : PrimeGenerator.v().express(denominator);
        return String.format("%s%s = %s%s", Utils.argString(to), s1, Utils.argString(from), s2);
    }

}