package org.lambd.obj;

public interface Location {
    Location combineWith(Location other);
    int getWeight();
    default void deepCopy(Location other) {}
}
