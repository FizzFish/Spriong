package org.lambd.transition;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.lambd.SpMethod;
import soot.Body;
import soot.Local;

import java.util.*;

public class VarRelation {
    Table<Integer, Integer, Weight> relations = HashBasedTable.create();
    private Map<Local, Integer> varMap = new HashMap<>();
    private int paramIndex;
    private SpMethod container;
    public VarRelation(SpMethod container) {
        this.container = container;
        Body body;
        try {
            body = container.getSootMethod().getActiveBody();
        } catch (Exception e) {
            return;
        }

        List<Local> parameters = body.getParameterLocals();
        for (int i = 0; i < parameters.size(); i++) {
            Local var = parameters.get(i);
            varMap.put(var, i);
            relations.put(i, i, Weight.ID);
        }
        paramIndex = parameters.size();
        if (!container.getSootMethod().isStatic()) {
            Local thisVar = body.getThisLocal();
            varMap.put(thisVar, -1);
            relations.put(-1, -1, Weight.ID);
        }
    }
    public int getVarIndex(Local val) {
        if (varMap.containsKey(val))
            return varMap.get(val);
        varMap.put(val, paramIndex ++);
        return paramIndex - 1;
    }
    public void update(Local fromVar, Local toVar, Weight w) {
        int from = getVarIndex(fromVar);
        int to = getVarIndex(toVar);
        List<Integer> rowsToUpdate = new ArrayList<>();
        relations.cellSet().stream()
                .filter(e -> e.getColumnKey() == from)
                .forEach(e -> rowsToUpdate.add(e.getRowKey()));
        Table<Integer, Integer, Weight> addRelations = HashBasedTable.create();
        rowsToUpdate.forEach(rowKey -> updateRelations(rowKey, from, to, w, addRelations));
        addRelations.cellSet().forEach(e -> {
            relations.put(e.getRowKey(), e.getColumnKey(), e.getValue());
        });
    }
    private void updateRelations(int i, int from, int to, Weight w, Table<Integer, Integer, Weight> addRelations) {
        Weight ow = relations.get(i, to);
        Weight w0 = relations.get(i, from);
        Weight nw = w.multiply(w0);
        if (w.hasEffect()) {
            // transition: y = x.w, x=argi.w0, y=argj.w1
            relations.cellSet().stream()
                    .filter(e -> e.getColumnKey() == to && e.getRowKey() != i)
                    .forEach(e -> {
                        // from = argi.w0, to = argj.w1, check whether w0 cover w1
                        Weight w1 = e.getValue();
                        if (w.isUpdate() || Weight.checkStore(w0, w1, w)) {
                            Weight uw = nw.divide(e.getValue());
                            container.getSummary().addTransition(i, e.getRowKey(), uw);
                        }

                    });
            // handle alias
        }
        // load: y=x.w, x=argi.w0, y=argj.w1
        if (Weight.checkLoad(w0, w)) {
            if (ow == null || nw.compareTo(ow) > 0)
                addRelations.put(i, to, nw);
        }
    }
    public void genSink(String sink, Weight w, Local var) {
        int index = getVarIndex(var);
        relations.cellSet().stream()
                .filter(e -> e.getColumnKey() == index)
                .forEach(e -> {
                    Weight nw = e.getValue().multiply(w);
                    if (!nw.hasOverField())
                        container.getSummary().addSink(e.getRowKey(), nw, sink);
                });
    }
    public void genReturn(Local retVar) {
        int index = getVarIndex(retVar);
        relations.cellSet().stream()
                .filter(e -> e.getColumnKey() == index)
                .forEach(e -> {
                    container.getSummary().addTransition(e.getRowKey(), -2, e.getValue());
                });
    }
}
