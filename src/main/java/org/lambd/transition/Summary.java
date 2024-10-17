package org.lambd.transition;

import org.lambd.SootWorld;
import org.lambd.SpMethod;
import org.lambd.utils.Pair;
import soot.RefType;
import soot.jimple.Stmt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 包含argtrans、sinktrans、rettrans三种摘要效果
 * updateCallers为了解决callgraph出现环的情况[TODO]
 */
public class Summary {
    private SpMethod container;
    private Map<String, Set<ArgTrans>> transitionMap = new HashMap<>();
    private Map<String, Set<SinkTrans>> sinkMap = new HashMap<>();
    private Set<RetTrans> retSet = new HashSet<>();
    private Set<SpMethod> updateCallers = new HashSet<>();
    public Summary(SpMethod container) {
        this.container = container;
    }
    public void addReturn(RefType type) {
        RetTrans rt = new RetTrans(type);
        retSet.add(rt);
    }
    public void addTransition(int from, int to, Weight w, Stmt stmt) {
        String key = String.format("%d,%d", from, to);
        Set<ArgTrans> transitions = transitionMap.computeIfAbsent(key, k -> new HashSet<>());
        if (transitions.stream().anyMatch(t -> t.getWeight().equals(w)))
            return;
        ArgTrans at = new ArgTrans(from, to, w, stmt);
        boolean added = transitions.add(at);
        if (added && !updateCallers.isEmpty())
            pushCallers();
    }
    public void addSink(String sink, int index, Weight weight, Stmt stmt) {
        String key = String.format("%s,%d", sink, index);
        SinkTrans st = new SinkTrans(sink, index, weight, stmt);
        boolean added = sinkMap.computeIfAbsent(key, k -> new HashSet<>()).add(st);
        if (added && !updateCallers.isEmpty())
            pushCallers();
    }
    public void apply(SpMethod method, Stmt stmt) {
        for (RetTrans rt : retSet) {
            rt.apply(method, stmt);
        }
        transitionMap.forEach((key, values) -> {
            for (ArgTrans value : values) {
                value.apply(method, stmt);
            }
        });
        sinkMap.forEach((key, values) -> {
            for (SinkTrans value : values) {
                value.apply(method, stmt);
            }
        });
    }
    public void addCaller(SpMethod caller) {
        updateCallers.add(caller);
    }
    public void pushCallers() {
        for (SpMethod caller : updateCallers)
            SootWorld.v().addCaller(caller);
        updateCallers.clear();
    }
    public boolean isEmpty() {
        return transitionMap.isEmpty() && sinkMap.isEmpty() && retSet.isEmpty();
    }
    public void print(boolean simple) {
        if (!simple ) {
            if (!isEmpty()) {
                System.out.println(container + ": ");
                transitionMap.forEach((key, values) -> {
                    System.out.printf("%s: %s\n", key, values);
                });
                sinkMap.forEach((key, values) -> {
                    System.out.printf("%s: %s\n", key, values);
                });
                retSet.forEach(value -> {
                    System.out.printf("%s: %s\n", "ret", value);
                });
            }
        } else if (!sinkMap.isEmpty()) {
            System.out.println(container + ": ");
            sinkMap.forEach((key, values) -> {
                values.forEach(value -> {
                    System.out.printf("%s: %s\n", key, value);
                });
            });
        }
    }
    public Map<String, Set<ArgTrans>> getTRansition() {
        return transitionMap;
    }
    public Map<String, Set<SinkTrans>> getSink() {
        return sinkMap;
    }
}
