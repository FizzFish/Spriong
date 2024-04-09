package org.lambd.obj;

import soot.Local;

public interface ObjManager {
    void copy(Local from, Local to, int update);
    void addObj(Local value, Location obj);
    void show();
}
