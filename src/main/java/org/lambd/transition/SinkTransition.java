package org.lambd.transition;

import org.lambd.SpMethod;
import org.lambd.obj.Fraction;
import soot.jimple.Stmt;

public class SinkTransition implements Transition {
    private String realSink;
    private int argIndex;
    private Fraction fraction;
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
        return "SinkRelation{" +
                "realSink='" + realSink + '\'' +
                ", argIndex=" + argIndex +
                ", fraction=" + fraction.details() +
                '}';
    }

    @Override
    public void apply(SpMethod method, Stmt stmt) {
        method.handleSink(stmt, this);
    }
}
