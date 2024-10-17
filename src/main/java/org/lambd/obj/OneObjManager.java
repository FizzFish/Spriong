package org.lambd.obj;

import org.lambd.SpMethod;
import org.lambd.pointer.*;
import org.lambd.utils.Utils;
import soot.Local;
import soot.SootField;
import soot.Type;
import soot.jimple.Stmt;

public class OneObjManager implements ObjManager {
    private final SpMethod container;
    private PointerToSet ptset;

    public OneObjManager(SpMethod container, PointerToSet ptset)
    {
        this.container = container;
        this.ptset = ptset;
    }

    /**
     * to = from: pointer(to) merge pointer(from)
     */
    @Override
    public void copy(Local from, Local to) {
        VarPointer fromPointer = ptset.getVarPointer(from);
        // 在某些情况下$stack对象可能与Soot优化相关，完全流敏感分析可能会导致漏报UnSound
        if (from.getName().startsWith("$")) // workingBuilder = $stack51;
            // merge to with from
            ptset.getSameVP(from, to);
        else {
            VarPointer toPointer = ptset.getVarPointer(to);
            ptset.copy(fromPointer, toPointer);
        }
    }

    /**
     * x = y.f: pointer(x) merge pointer(y.f)
     */
    public void loadField(Local to, Local base, SootField field) {
        ptset.getLocalObjs(base).forEach(obj -> {
            InstanceField fromPointer = ptset.getInstanceField(obj, field);
            VarPointer toPointer = ptset.getVarPointer(to);
            ptset.copy(fromPointer, toPointer);
        });
    }

    /**
     * x = C.f: pointer(x) merge pointer(C.f)
     */
    public void loadStaticField(Local to, Class clazz, SootField field) {
        // x = C.f
        StaticField fromPointer = ptset.getStaticField(field);
        VarPointer toPointer = ptset.getVarPointer(to);
        ptset.copy(fromPointer, toPointer);
    }

    /**
     * x.f = y: pointer(x.f) merge pointer(y)
     * need alias analysis
     * 其实Spriong已经是Obj敏感的了，但是对于跨函数的FormatObj的field写还是应该记录下来，因为会影响caller的数据流关系
     * 而对于普通的Obj则不需要
     */
    public void storeField(Local base, SootField field, Local from, Stmt stmt) {
        VarPointer fromPointer = ptset.getVarPointer(from);
        ptset.getLocalObjs(base).forEach(obj -> {
            InstanceField toPointer = ptset.getInstanceField(obj, field);
            if (obj instanceof FormatObj formatObj)
                ptset.storeAlias(fromPointer, formatObj, field, stmt);
            ptset.copy(fromPointer, toPointer);
        });
    }

    /**
     * C.f = y: pointer(C.f) merge pointer(y)
     */
    public void storeStaticField(Class clazz, SootField field, Local from) {
        VarPointer fromPointer = ptset.getVarPointer(from);
        StaticField toPointer = ptset.getStaticField(field);
        ptset.copy(fromPointer, toPointer);
    }

    /**
     * x = y[i]: pointer(x) merge pointer(y[i])
     * 不关注数组索引，而是将y[]视作一个类似field的pointer，这里我们将这个数组field标记为Field("[*]")
     */
    public void loadArray(Local to, Local base) {
        ptset.getLocalObjs(base).forEach(obj -> {
//            assert obj instanceof ArrayObj;
            InstanceField fromPointer = ptset.getInstanceField(obj, Utils.arrayField);
            VarPointer toPointer = ptset.getVarPointer(to);
            ptset.copy(fromPointer, toPointer);
        });
    }

    /**
     * x[i] = y: pointer(x[i]) merge pointer(y)
     * 同样，理论上也需要进行alias分析
     */
    public void storeArray(Local base, Local from, Stmt stmt) {
        // x[i] = y
        ptset.getLocalObjs(base).forEach(obj -> {
            VarPointer fromPointer = ptset.getVarPointer(from);
            if (obj instanceof FormatObj formatObj)
                ptset.storeAlias(fromPointer, formatObj, Utils.arrayField, stmt);
            InstanceField toPointer = ptset.getInstanceField(obj, Utils.arrayField);
            ptset.copy(fromPointer, toPointer);
        });
    }

}
