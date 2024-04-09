package org.lambd.obj;

import org.lambd.SpMethod;
import soot.Local;

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
}
