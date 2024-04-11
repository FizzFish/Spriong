package org.lambd.transition;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.commons.math3.fraction.Fraction;
import org.lambd.SpMethod;
import soot.jimple.Stmt;

import java.util.ArrayList;
import java.util.List;

public class MethodSummary {
    Table<Integer, Integer, Weight> transitionMap = HashBasedTable.create();
    Table<String, Integer, Fraction> sinkMap = HashBasedTable.create();
    List<Transition> transitions = new ArrayList<>();
    private SpMethod method;
    public MethodSummary(SpMethod method) {
        this.method = method;
    }
    public void addTransition(Transition transition) {
        boolean add = false;
        if (transition instanceof WTransition nt) {
            int from = nt.from();
            int to = nt.to();
            if (transitionMap.contains(from, to)) {
                if (nt.w().compareTo(transitionMap.get(from, to)) > 0) {
                    transitionMap.put(from, to, nt.w());
                    add = true;
                }
            } else {
                transitionMap.put(from, to, nt.w());
                add = true;
            }
        } else if (transition instanceof SinkTransition st) {
            int index = st.getIndex();
            String sink = st.getSink();
            if (sinkMap.contains(sink, index)) {
                if (st.getFraction().compareTo(sinkMap.get(sink, index)) > 0) {
                    sinkMap.put(sink, index, st.getFraction());
                    add = true;
                }
            } else {
                sinkMap.put(sink, index, st.getFraction());
                add = true;
            }
        }
        if (add)
            transitions.add(transition);
    }
    public boolean isEmpty() {
        return transitions.isEmpty();
    }
    public void apply(SpMethod method, Stmt stmt) {
        transitions.forEach(t -> t.apply(method, stmt));
    }
    public void print() {
        System.out.println(method.getName() + ": ");
        for (Transition t : transitions)
            System.out.println(t);
    }
}



