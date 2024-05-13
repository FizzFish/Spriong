package org.lambd.transition;

import org.lambd.SpMethod;
import org.lambd.utils.Utils;
import soot.jimple.Stmt;

public class SinkTrans implements Transition {
    private String sinkDes;
    private int argIndex;
    private Weight weight;
    // method.param(index) -(fraction)-> sink
    private int calleeID;
    public SinkTrans(String sink, int index, Weight weight, int calleeID)
    {
        this.argIndex = index;
        this.weight = weight;
        this.sinkDes = sink;
        this.calleeID = calleeID;
    }
    public SinkTrans(String sink, int index, Weight weight)
    {
        this(sink, index, weight, -1);
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
        return String.format("%s;%d", weight, calleeID);
    }
    @Override
    public void apply(SpMethod method, Stmt stmt) {
        method.handleSink(stmt, sinkDes, argIndex, weight, 0);
    }
    public void apply(SpMethod method, Stmt stmt, int calleeID) {
        method.handleSink(stmt, sinkDes, argIndex, weight, calleeID);
    }
}
