package org.lambd.obj;

import org.lambd.SpMethod;
import soot.Local;

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
            addObj(to, objMap.get(from));
        }
    }

    @Override
    public void addObj(Local value, Location obj) {
        if (objMap.containsKey(value)) {
            Location newOne = objMap.get(value).combineWith(obj);
            objMap.put(value, newOne);
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
}
