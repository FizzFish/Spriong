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
    public void addTransition(int from, int to, Weight w) {
        if (transitionMap.contains(from, to) && w.compareTo(transitionMap.get(from, to)) <= 0)
            return;
        transitionMap.put(from, to, w);
        ArgTrans transition = new ArgTrans(from, to, w);
        transitions.add(transition);
    }
    public void addSink(int index, Fraction fraction, String sink) {
        if (sinkMap.contains(sink, index) && fraction.compareTo(sinkMap.get(sink, index)) <= 0)
            return;
        sinkMap.put(sink, index, fraction);
        SinkTrans st = new SinkTrans(index, fraction, sink);
        transitions.add(st);
    }
    public boolean isEmpty() {
        return transitions.isEmpty();
    }
    public void apply(SpMethod method, Stmt stmt) {
        transitions.forEach(t -> t.apply(method, stmt));
    }
    public void print(boolean simple) {
        if (simple && transitions.stream().anyMatch(t -> !t.isReturnTrans())) {
            System.out.println(method + ": ");
            transitions.stream().filter(t -> !t.isReturnTrans()).forEach(t -> System.out.println("\t" + t));
        } else if (!simple && !transitions.isEmpty()) {
            System.out.println(method + ": ");
            for (Transition t : transitions)
                System.out.println("\t" + t);
        }
    }
}



