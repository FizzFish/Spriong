package org.lambd.transformer;

import org.lambd.SpMethod;
import org.lambd.condition.Condition;
import soot.Unit;
import soot.jimple.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpStmt {
    private SpMethod container;
    private Stmt stmt;
    private int index;
    private Condition condition;
    public SpStmt(SpMethod container, Stmt stmt, int index) {
        this.container = container;
        this.stmt = stmt;
        this.index = index;
        this.condition = Condition.ROOT;
    }
    public Stmt getStmt() {
        return stmt;
    }
    public int getIndex() {
        return index;
    }
    public SpMethod getContainer() {
        return container;
    }
    public Condition getCondition() {
        return condition;
    }

}
