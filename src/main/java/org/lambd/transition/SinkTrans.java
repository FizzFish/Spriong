package org.lambd.transition;

import org.apache.commons.math3.fraction.Fraction;
import org.lambd.SpMethod;
import org.lambd.utils.PrimeGenerator;
import org.lambd.utils.Utils;
import soot.jimple.Stmt;

public class SinkTrans implements Transition {
    private String realSink;
    private int argIndex;
    private Weight weight;
    // method.param(index) -(fraction)-> sink
    public SinkTrans(int index, Weight weight, String sink)
    {
        this.argIndex = index;
        this.weight = weight;
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
    public Weight getWeight()
    {
        return weight;
    }
    public String toString() {
        return String.format("<sink>: %s.%s flow to %s", Utils.argString(argIndex), weight.getNegative(), realSink);
    }
    @Override
    public void apply(SpMethod method, Stmt stmt) {
        method.handleSink(stmt, this);
    }
}
