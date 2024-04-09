package org.lambd.obj;

import org.lambd.SpMethod;
import soot.Local;
import soot.SootField;
import soot.SootMethod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MulObjManager implements ObjManager {
    private Map<Local, Set<Location>> objMap = new HashMap<>();
    private SpMethod method;
    public MulObjManager(SpMethod method) {
        this.method = method;
    }
    public Map getObjMap() {
        return objMap;
    }


    @Override
    public void copy(Local from, Local to) {

    }

    @Override
    public void copy(Local from, Local to, Relation relation) {

    }

    @Override
    public void show() {
        for (Map.Entry<Local, Set<Location>> entry : objMap.entrySet()) {
            Local key = entry.getKey();
            Set<Location> value = entry.getValue();
            System.out.printf("%s => %s\n", key, value);
        }
    }

    public void addObj(Local value, Location obj) {
        addObj(value, obj, 0);
    }
    private void addObj(Local value, Location obj, int update)
    {
        if (objMap.containsKey(value))
        {
            Set<Location> objSet = objMap.get(value);
            if (update == 1) {
                objSet.forEach(o -> {
                    o.deepCopy(obj);
                });
            } else {
                boolean success = objMap.get(value).add(obj);
                if (success)
                    ;
            }
        }
        else
        {
            Set set = new HashSet<>();
            set.add(obj);
            objMap.put(value, set);
        }
    }
    public void copy(Local from, Local to, int update) {
        if (objMap.containsKey(from)) {
            Set<Location> objs = objMap.get(from);
            objs.forEach(obj -> {
                addObj(to, obj, update);
            });
        }
    }
    public void loadField(Local to, Local base, SootField field) {
        // x = y.f




    }
    public void loadStaticField(Local to, Class clazz, SootField field) {
        // x = C.f

    }

    public void storeField(Local base, SootField field, Local from) {
        // x.f = y

    }
    public void storeStaticField(Class clazz, SootField field, Local from) {
        // C.f = y

    }
    public void loadArray(Local to, Local base) {
        // x = y[i]

    }
    public void storeArray(Local base, Local from) {
        // x[i] = y

    }
    public void invoke(Local to, Local base, SootMethod method) {

    }
}
