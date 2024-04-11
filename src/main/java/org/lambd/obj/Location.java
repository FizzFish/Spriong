package org.lambd.obj;

import org.lambd.SootWorld;
import org.lambd.SpMethod;
import org.lambd.transition.WTransition;
import org.lambd.transition.Transition;
import org.lambd.transition.Weight;

public interface Location {
    Location combineWith(Location other);
    boolean exposed();
    int getIndex();
    SpMethod getMethod();
}
