package org.lambd.transition;

import org.lambd.SpMethod;
import org.lambd.transformer.SpStmt;
import soot.jimple.Stmt;

/**
 * 数据流配置：from-kind->to
 * kind = 1: real copy byte
 */
public class BaseTransition implements Transition {
    private int from;
    private int to;
    private int kind;
    public BaseTransition(int from, int to, int kind) {
        this.from = from;
        this.to = to;
        this.kind = kind;
    }
    public String toString() {
        return "(" + from + ", " + to + ", " + kind +")";
    }
    @Override
    public void apply(SpMethod method, SpStmt stmt) {
        // kind: 0->pointer copy; 1->deep copy; 2->list.add; 3->iterator(); 4->iterator.next
        if (kind < 2)
            method.handleTransition(stmt, from, to, kind == 0 ? Weight.ONE : Weight.COPY);
        else
            method.handleCollectionOp(stmt, from, to, kind);
    }
}
