package org.lambd.obj;

import org.lambd.SootWorld;
import org.lambd.SpMethod;
import org.lambd.transition.FieldTransition;
import org.lambd.transition.Transition;

public interface Location {
    Location combineWith(Location other);
    int getWeight();
    boolean exposed();
    int getIndex();
    SpMethod getMethod();
    Fraction getFraction();
    default void deepCopy(Location other) {
        if (exposed() && other.exposed()) {
            int fromIndex = other.getIndex();
            int toIndex = getIndex();
            if (fromIndex == toIndex)
                return;
            Fraction fraction = Fraction.multiply(other.getFraction(), getFraction());
            Relation relation = new Relation(fraction, true);
            Transition transition = new FieldTransition(fromIndex, toIndex, relation);
            System.out.println(getMethod() + " " + transition);
            SootWorld.v().addTransition(getMethod().getSootMethod(), transition);
        }
    }
}
