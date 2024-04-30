package org.lambd.transition;

import org.lambd.SpMethod;
import org.lambd.utils.Pair;
import soot.jimple.Stmt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Summary {
    private SpMethod container;
    private Map<String, Set<Weight>> transitionMap = new HashMap<>();
    private Map<String, Set<Weight>> sinkMap = new HashMap<>();
    public Summary(SpMethod container) {
        this.container = container;
    }
    public void addTransition(int from, int to, Weight w) {
        String key = String.format("%d,%d", from, to);
        transitionMap.computeIfAbsent(key, k -> new HashSet<>()).add(w);
    }
    public void addSink(int index, Weight weight, String sink) {
        String key = String.format("%s,%d", sink, index);
        sinkMap.computeIfAbsent(key, k -> new HashSet<>()).add(weight);
    }
    public void apply(SpMethod method, Stmt stmt) {
        transitionMap.forEach((key, values) -> {
            String[] split = key.split(",");
            int from = Integer.parseInt(split[0]);
            int to = Integer.parseInt(split[1]);
            for (Weight value : values) {
                method.handleTransition(stmt, from, to, value);
            }
        });
        sinkMap.forEach((key, values) -> {
            String[] split = key.split(",");
            int index = Integer.parseInt(split[1]);
            for (Weight value : values) {
                method.handleSink(stmt, split[0], index, value);
            }
        });
    }
    public boolean isEmpty() {
        return transitionMap.isEmpty() && sinkMap.isEmpty();
    }
    public void print(boolean simple) {
        if (!simple) {
            if (!transitionMap.isEmpty() || !sinkMap.isEmpty()) {
                System.out.println(container + ": ");
                transitionMap.forEach((key, values) -> {
                    System.out.printf("%s: %s\n", key, values);
                });
                sinkMap.forEach((key, values) -> {
                    System.out.printf("%s: %s\n", key, values);
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
}