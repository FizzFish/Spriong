package org.lambd.transition;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.lambd.SpMethod;
import org.lambd.utils.Utils;
import soot.jimple.Stmt;

import java.util.stream.Collectors;

public class MethodSummary {
    Table<Integer, Integer, Weight> transitionMap = HashBasedTable.create();
    Table<String, Integer, Weight> sinkMap = HashBasedTable.create();
    private SpMethod method;
    public MethodSummary(SpMethod method) {
        this.method = method;
    }
    public void addTransition(int from, int to, Weight w) {
        if (transitionMap.contains(from, to)) {
            if (w.compareTo(transitionMap.get(from, to)) <= 0)
                return;
//            transitionMap.put(from, to, w);
//            return;
        }
        transitionMap.put(from, to, w);
    }
    public void addSink(int index, Weight weight, String sink) {
        if (sinkMap.contains(sink, index)) {
            if (weight.compareTo(sinkMap.get(sink, index)) <= 0)
                return;
            sinkMap.put(sink, index, weight);
            return;
        }
        sinkMap.put(sink, index, weight);
    }
    public boolean isEmpty() {
        return transitionMap.isEmpty() && sinkMap.isEmpty();
    }
    public void apply(SpMethod method, Stmt stmt) {
        transitionMap.cellSet().forEach(cell -> method.handleTransition(stmt, cell.getRowKey(), cell.getColumnKey(), cell.getValue()));
        sinkMap.cellSet().forEach(cell -> method.handleSink(stmt, cell.getRowKey(), cell.getColumnKey(), cell.getValue()));
    }
    public void print(boolean simple) {
        boolean printMethod = false;
        if (!simple && transitionMap.cellSet().stream().anyMatch(cell -> cell.getColumnKey() != -2)) {
            System.out.println(method + ": ");
            printMethod = true;
            transitionMap.cellSet().stream().filter(cell -> cell.getColumnKey() != -2).forEach(cell ->  {
                System.out.printf("%s.%s = %s.%s\n", Utils.argString(cell.getColumnKey()), cell.getValue().getNegative(), Utils.argString(cell.getRowKey()), cell.getValue().getPositive());
            });
        }
        if (!sinkMap.isEmpty()) {
            if (!printMethod)
                System.out.println(method + ": ");
            sinkMap.cellSet().forEach(cell ->  {
                System.out.printf("\t<sink>: %s.%s/%s flow to %s\n", Utils.argString(cell.getColumnKey()), cell.getValue().getPositive(), cell.getValue().getNegative(), cell.getRowKey());
            });
        }
    }
}



