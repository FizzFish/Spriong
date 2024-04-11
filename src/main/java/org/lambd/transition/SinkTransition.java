package org.lambd.transition;

import org.apache.commons.math3.fraction.Fraction;
import org.lambd.SpMethod;
import org.lambd.utils.PrimeGenerator;
import org.lambd.utils.Utils;
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
        int numerator = fraction.getNumerator();
        int denominator = fraction.getDenominator();
        String s1 = numerator == 1? "" : PrimeGenerator.v().express(numerator);
        String s2 = denominator == 1? "" : PrimeGenerator.v().express(denominator);
//        return String.format("Sink@%d: %s/%s, %s", argIndex, s1, s2, realSink);
        return String.format("%s%s sink to %s", Utils.argString(argIndex), s2, realSink);
    }
    @Override
    public void apply(SpMethod method, Stmt stmt) {
        method.handleSink(stmt, this);
    }
}
