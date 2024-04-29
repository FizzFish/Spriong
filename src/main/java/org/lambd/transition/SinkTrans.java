package org.lambd.transition;

import org.lambd.SpMethod;
import org.lambd.utils.Utils;
import soot.jimple.Stmt;

public class SinkTrans implements Transition {
    private String sinkDes;
    private int argIndex;
    private Weight weight;
    // method.param(index) -(fraction)-> sink
    public SinkTrans(int index, Weight weight, String sink)
    {
        this.argIndex = index;
        this.weight = weight;
        this.sinkDes = sink;
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
        return String.format("<sink>: %s.%s flow to %s", Utils.argString(argIndex), weight, sinkDes);
    }
    @Override
    public void apply(SpMethod method, Stmt stmt) {
        method.handleSink(stmt, sinkDes, argIndex, weight);
    }

    @Override
    public boolean debugTrans() {
        return true;
    }
}
