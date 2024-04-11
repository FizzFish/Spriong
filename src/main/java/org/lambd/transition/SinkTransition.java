package org.lambd.transition;

import org.apache.commons.math3.fraction.Fraction;
import org.lambd.SpMethod;
import org.lambd.utils.PrimeGenerator;
import soot.jimple.Stmt;

public class SinkTransition implements Transition {
    private String realSink;
    private int argIndex;
    private Fraction fraction;
    // method.param(index) -(fraction)-> sink
    public SinkTransition(int index, Fraction fraction, String sink)
    {
        this.argIndex = index;
        this.fraction = fraction;
        this.realSink = sink;
    }
    public String getSink()
    {
        return realSink;
    }
    public int getIndex()
    {
        return argIndex;
    }
    public Fraction getFraction()
    {
        return fraction;
    }
    public String toString() {
        String s1 = PrimeGenerator.v().primeFactorization(fraction.getNumerator()).toString();
        String s2 = PrimeGenerator.v().primeFactorization(fraction.getDenominator()).toString();
        return String.format("Sink@%d: %s, %s/%s", argIndex, realSink, s1, s2);
    }
    @Override
    public void apply(SpMethod method, Stmt stmt) {
        method.handleSink(stmt, this);
    }
}
