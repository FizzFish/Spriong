package org.lambd.obj;

import org.lambd.SpMethod;
import soot.Local;
import soot.SootField;
import soot.SootMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OneObjManager implements ObjManager {
    private final SpMethod method;
    private Map<Local, Location> objMap = new HashMap<>();

    public OneObjManager(SpMethod method)
    {
        this.method = method;
    }

    @Override
    public void copy(Local from, Local to, int update) {
        if (objMap.containsKey(from)) {
            Location obj = objMap.get(from);
            addObj(to, objMap.get(from), update);
        }
    }

    @Override
    public void addObj(Local value, Location obj) {
        addObj(value, obj, 0);
    }
    public void addObj(Local value, Location obj, int update) {
        if (objMap.containsKey(value)) {
            Location oldOne = objMap.get(value);
            if (update == 1)
                oldOne.deepCopy(obj);
            else {
                Location newOne = oldOne.combineWith(obj);
                objMap.put(value, newOne);
            }
        } else {
            objMap.put(value, obj);
        }
    }

    @Override
    public void show() {
        for (Map.Entry<Local, Location> entry : objMap.entrySet()) {
            Local key = entry.getKey();
            Location value = entry.getValue();
            System.out.printf("%s => %s\n", key, value);
        }
    }
    public void loadField(Local to, Local base, SootField field) {
        // x = y.f
        Location obj = objMap.get(base);
        AccessPath accessPath = new AccessPath(obj, field, false);
        addObj(to, accessPath);
    }
    public void loadStaticField(Local to, Class clazz, SootField field) {
        // x = C.f

    }

    public void storeField(Local base, SootField field, Local from) {
        // x.f = y
        Location obj = objMap.get(from);
        AccessPath accessPath = new AccessPath(obj, field, true);
        addObj(base, accessPath);
    }
    public void storeStaticField(Class clazz, SootField field, Local from) {
        // C.f = y

    }
    public void loadArray(Local to, Local base) {
        // x = y[i]
        Location obj = objMap.get(base);
        AccessPath accessPath = new AccessPath(obj, "array", false);
        addObj(to, accessPath);
    }
    public void storeArray(Local base, Local from) {
        // x[i] = y
        Location obj = objMap.get(from);
        AccessPath accessPath = new AccessPath(obj, "array", true);
        addObj(base, accessPath);
    }
    public void invoke(Local to, Local base, SootMethod method) {

    }
}
