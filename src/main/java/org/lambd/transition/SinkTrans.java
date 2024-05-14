package org.lambd.transition;

import org.lambd.SpMethod;
import org.lambd.utils.Utils;
import soot.jimple.Stmt;

public class SinkTrans implements Transition {
    private String sinkDes;
    private int argIndex;
    private Weight weight;
    // method.param(index) -(fraction)-> sink
    private Stmt born;
    public SinkTrans(String sink, int index, Weight weight, Stmt born)
    {
        this.argIndex = index;
        this.weight = weight;
        this.sinkDes = sink;
        this.born = born;
    }
    public SinkTrans(String sink, int index, Weight weight)
    {
        this(sink, index, weight, null);
    }
    public boolean equals(Object obj) {
        if (obj instanceof SinkTrans st)
            return weight.equals(st.weight);
        return false;
    }
    public int hashCode() {
        return weight.hashCode();
    }
    public String getSink()
    {
        return sinkDes;
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
//        return String.format("<sink>: %s.%s flow to %s", Utils.argString(argIndex), weight, sinkDes);
        return String.format("%s", weight);
    }
    @Override
    public void apply(SpMethod caller, Stmt stmt) {
        caller.handleSink(stmt, sinkDes, argIndex, weight);
    }
}
