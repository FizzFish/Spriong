package org.lambd.transformer;

import org.lambd.SpMethod;
import soot.Unit;
import soot.jimple.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Converter {
    static Map<Stmt, SpStmt> stmtMap = new HashMap<>();
    public static void transformer(SpMethod container) {
        int index = 0;
        for (Unit unit : container.getSootMethod().getActiveBody().getUnits()) {
            Stmt stmt = (Stmt) unit;
            if (stmt instanceof AssignStmt || stmt instanceof InvokeStmt
                    || stmt instanceof ReturnStmt || stmt instanceof IfStmt
                    || stmt instanceof LookupSwitchStmt || stmt instanceof TableSwitchStmt) {
                SpStmt spStmt = new SpStmt(container, stmt, index);
                stmtMap.put(stmt, spStmt);
                container.addStmts(spStmt);
            }
            index++;
        }
    }
    public static SpStmt convert(Stmt stmt, SpMethod container, int index) {
        SpStmt spStmt = new SpStmt(container, stmt, index);
        stmtMap.put(stmt, spStmt);
        container.addStmts(spStmt);
        return spStmt;
    }

    public static SpStmt getSmt(Stmt stmt) {
        return stmtMap.get(stmt);
    }
}
