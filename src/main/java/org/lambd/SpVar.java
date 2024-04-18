package org.lambd;

import org.apache.commons.math3.fraction.Fraction;
import org.lambd.transition.SinkTrans;
import org.lambd.transition.Weight;
import soot.Local;
import soot.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpVar {
    private Local var;
    // this, param0..., paramN, ClassType, type0..., typeN
    Map<Type, Integer> typeMap = new HashMap<>();
    List<Weight> paramWeight;
    private int locationSize;
    private int paramSize;
    private SpMethod container;
    public SpVar(SpMethod container, Local var) {
        this.var = var;
        this.container = container;
        paramSize = container.getSootMethod().getParameterCount() + 1;

        List<Type> typeList = container.getGenTypes();
        int count = 0;
        for (Type type : typeList) {
            typeMap.put(type, count + paramSize);
            count ++;
        }
        locationSize = paramSize + typeList.size();
        paramWeight = new ArrayList<>(locationSize);
        for (int i = 0; i < locationSize; i++) {
            paramWeight.add(Weight.ZERO);
        }
    }
    public SpVar(SpMethod method, Local var, int paramIndex) {
        this(method, var);
        paramWeight.set(paramIndex + 1, Weight.ID);
    }
    // var = new Type()
    public void assignObj(Type type) {
        int i = typeMap.get(type);
        if (Weight.ONE.compareTo(paramWeight.get(i)) > 0) {
            paramWeight.set(i, Weight.ID);
        }
    }
    public void copy(SpVar other, Weight w) {
        for (int i = 0; i < locationSize; i++) {
            Weight nw = other.paramWeight.get(i).multiply(w);
            Weight ow = paramWeight.get(i);
            if (nw.compareTo(ow) > 0)
                paramWeight.set(i, nw);
        }
    }
    public void update(SpVar other, Weight w) {
        if (w.isUpdate()) {
            for (int i = 0; i < paramSize; i++)
                for (int j = 0; j < paramSize; j++) {
                    Weight wi = paramWeight.get(i);
                    Weight oj = other.paramWeight.get(j);
                    if (i != j && !wi.isZero() && !oj.isZero()) {
                        Weight nw = oj.multiply(w).divide(wi);
                        nw.setUpdate(true);
                        container.getSummary().addTransition(j-1, i-1, nw);
                        // handle alias ?
                    }
                }
//            return;
        }
        for (int i = 0; i < locationSize; i++) {
            Weight nw = other.paramWeight.get(i).multiply(w);
            Weight ow = paramWeight.get(i);
            if (nw.compareTo(ow) > 0)
                paramWeight.set(i, nw);
        }
    }
    public void genSink(SinkTrans sink) {
        for (int i = 0; i < paramSize; i++) {
            Weight ow = paramWeight.get(i);
            if (!ow.isZero()) {
                Weight weight = ow.multiply(sink.getWeight());
                container.getSummary().addSink(i-1, weight, sink.getSink());

            }
        }
    }

    public String toString() {
        return var.toString();
    }
    public List<Weight> getParamWeight() {
        return paramWeight;
    }

}
