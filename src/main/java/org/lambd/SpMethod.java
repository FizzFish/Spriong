package org.lambd;

import org.lambd.obj.FormatObj;
import org.lambd.obj.Obj;
import org.lambd.transition.Transition;
import soot.*;

import java.util.*;

public class SpMethod {
    private final List<Type> paramTypes;
    private final SootClass clazz;
    private final Type returnType;
    public final String name;
    private final List<Local> paramters;
    private final Local thisVar;
    private Map<Value, Set<Obj>> objMap = new HashMap<>();
    private List<Transition> transitions = new ArrayList<>();
    public SpMethod(SootMethod sootMethod) {
        this.paramTypes = sootMethod.getParameterTypes();
        this.clazz = sootMethod.getDeclaringClass();
        this.returnType = sootMethod.getReturnType();
        this.name = sootMethod.getName();
        this.paramters = sootMethod.getActiveBody().getParameterLocals();

        for (int i = 0; i < paramTypes.size(); i++) {
            addObj(paramters.get(i), new FormatObj(paramTypes.get(i), this, i));
        }
        if (!sootMethod.isStatic()) {
            this.thisVar = sootMethod.getActiveBody().getThisLocal();
            addObj(thisVar, new FormatObj(clazz.getType(), this, -1));
        } else {
            this.thisVar = null;
        }
    }
    public void addObj(Value value, Obj obj)
    {
        if (objMap.containsKey(value))
        {
            objMap.get(value).add(obj);
        }
        else
        {
            Set set = new HashSet<>();
            set.add(obj);
            objMap.put(value, set);
        }
    }
    public void addObjSet(Value value, Set<Obj> objSet)
    {
        if (objMap.containsKey(value))
        {
            objMap.get(value).addAll(objSet);
        }
        else
        {
            objMap.put(value, Set.copyOf(objSet));
        }
    }
    public void addTransition(Transition transition)
    {
        transitions.add(transition);
    }
    public List<Transition> getTransitions()
    {
        return transitions;
    }
    public Map<Value, Set<Obj>> getPts() {
        return objMap;
    }
    public String getName() {
        return name;
    }
    public void copy(Local from, Local to) {
        // field ?
        // deep or shallow ?
        if (objMap.containsKey(from)) {
            Set<Obj> objs = objMap.get(from);
            objs.forEach(obj -> {
                addObj(to, obj);
            });
        }
    }
    public void loadField(Local to, Local base, SootField field) {
        // x = y.f

    }
    public void loadStaticField(Local to, Class clazz, SootField field) {
        // x = C.f

    }

    public void storeField(Local base, SootField field, Value from) {
        // x.f = y

    }
    public void storeStaticField(Class clazz, SootField field, Value from) {
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
