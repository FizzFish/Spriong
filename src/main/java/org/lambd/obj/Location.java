package org.lambd.obj;

import org.lambd.SpMethod;

public interface Location {
    Location combineWith(Location other);
    boolean exposed();
    int getIndex();
    SpMethod getMethod();
}
