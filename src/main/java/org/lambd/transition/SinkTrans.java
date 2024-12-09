package org.lambd.transition;

import org.lambd.SootWorld;
import org.lambd.SpMethod;
import org.lambd.transformer.SpStmt;
import org.lambd.utils.Utils;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.jimple.Stmt;

/**
 * 代表某个参数param-index与污点信息sinkDes相关，权重是weight
 */
public class SinkTrans implements Transition {
    private String sinkDes;
    private int argIndex;
    private Weight weight;
    // method.param(index) -(fraction)-> sink
    private SpStmt born;
    public SinkTrans(String sink, int index, Weight weight, SpStmt born)
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
    public void apply(SpMethod caller, SpStmt stmt) {
        SootMethodRef methodRef = stmt.getStmt().getInvokeExpr().getMethodRef();
        String signature = methodRef.getSignature();
        if (signature.contains(sinkDes)) {
            NeoGraph graph = SootWorld.v().getGraph();
            SootMethod sm = caller.getSootMethod();
            SootWorld.v().getGraph().addSink(methodRef.getName(), signature, "sink:  " + argIndex);
            graph.updateNeo4jRelation(sm.getName(), sm.getSignature(), methodRef.getName(), signature);
        }
        caller.handleSink(stmt, sinkDes, argIndex, weight);
    }
}
