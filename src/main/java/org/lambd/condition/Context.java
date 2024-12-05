package org.lambd.condition;

import org.lambd.SpMethod;
import org.lambd.pointer.PointerToSet;
import soot.Local;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// context是一系列Constraint的组合，不同context下的函数的Summary是不一样的
public class Context {
    private SpMethod container;
    private List<Constraint> constraints = new ArrayList<>();
    public Context(SpMethod container) {
        this.container = container;
    }
    public void addConstraint(Constraint c) {
        constraints.add(c);
    }
    public boolean satisfy(Map<Integer, Local> varMap) {
        PointerToSet pts = container.getPtset();
        for (Constraint c : constraints) {
            if (!c.satisfy(varMap, pts)) {
                return false;
            }
        }
        return true;
    }

}
