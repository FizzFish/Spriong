package org.lambd.transition;

import org.lambd.SpMethod;
import org.lambd.transformer.SpStmt;
import soot.RefType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Effect {
    private Map<String, Set<ArgTrans>> transitionMap = new HashMap<>();
    private Map<String, Set<SinkTrans>> sinkMap = new HashMap<>();
    private Set<RetTrans> retSet = new HashSet<>();

    public void addReturn(RefType type) {
        RetTrans rt = new RetTrans(type);
        retSet.add(rt);
    }
    public void addTransition(int from, int to, Weight w, SpStmt stmt) {
        String key = String.format("%d,%d", from, to);
        Set<ArgTrans> transitions = transitionMap.computeIfAbsent(key, k -> new HashSet<>());
//        if (transitions.stream().anyMatch(t -> t.getWeight().equals(w)))
//            return;
        ArgTrans at = new ArgTrans(from, to, w, stmt);
        transitions.add(at);
    }
    public void addSink(String sink, int index, Weight weight, SpStmt stmt) {
        String key = String.format("%s,%d", sink, index);
        SinkTrans st = new SinkTrans(sink, index, weight, stmt);
        sinkMap.computeIfAbsent(key, k -> new HashSet<>()).add(st);
    }
    public Map<String, Set<ArgTrans>> getTransition() {
        return transitionMap;
    }
    public Map<String, Set<SinkTrans>> getSink() {
        return sinkMap;
    }
    public void apply(SpMethod method, SpStmt stmt) {
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
    public void print(boolean simple, String container) {
        if (!sinkMap.isEmpty()) {
            System.out.println(container + ": ");
            sinkMap.forEach((key, values) -> {
                values.forEach(value -> {
                    System.out.printf("\t%s: %s\n", key, value);
                });
            });
        }
    }
}
