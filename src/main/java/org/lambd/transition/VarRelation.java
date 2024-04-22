package org.lambd.transition;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.lambd.SpMethod;
import soot.Body;
import soot.Local;
import soot.Type;
import soot.Value;
import soot.jimple.AnyNewExpr;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;

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
        rowsToUpdate.forEach(rowKey -> updateRelations(rowKey, from, to, w));
    }
    private void updateRelations(int i, int from, int to, Weight w) {
        Weight ow = relations.get(i, to);
        Weight nw = relations.get(i, from).multiply(w);

        if (ow == null || nw.compareTo(ow) > 0)
            relations.put(i, to, nw);
        if (w.isUpdate()) {
//            List<Integer> aliasList = new ArrayList<>();
            relations.cellSet().stream()
                    .filter(e -> e.getColumnKey() == to && e.getRowKey() != i)
                    .forEach(e -> {
                    Weight uw = nw.divide(e.getValue());
                    uw.setUpdate(true);
//                    aliasList.add(e.getRowKey());
                    container.getSummary().addTransition(i, e.getRowKey(), uw);
                });
            // handle alias
        }
    }
    public void genSink(SinkTrans sink, Local var) {
        int index = getVarIndex(var);
        relations.cellSet().stream()
                .filter(e -> e.getColumnKey() == index)
                .forEach(e -> {
                    container.getSummary().addSink(e.getRowKey(), e.getValue().multiply(sink.getWeight()), sink.getSink());
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
