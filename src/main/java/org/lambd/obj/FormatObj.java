package org.lambd.obj;

import com.google.common.collect.Maps;
import org.lambd.condition.Condition;
import org.lambd.pointer.InstanceField;
import org.lambd.transformer.SpStmt;
import soot.SootField;
import soot.Type;

import javax.annotation.Nullable;
import java.util.*;

/**
 * FormatObj代表一个参数抽象对象
 * 在一个context调用下，他会关联一个实际的Obj
 * type可能是一个不确定的类型，因此在formatObj.invoke时，可能需要判断它的子类
 * 但是当cast(formatObj)或transfer后，可以认为他的类型是确定的了
 * 当函数调用时，base.invoke(args...)时会生成多个参数对象，编号为-1,0,1...
 */
public class FormatObj extends Obj implements Index {
    private int index;
    // 在一个上下文中，应该只有一个实际对象
    private Set<RealObj> realObjs = new HashSet<>();
    protected List<SootField> fields = new ArrayList<>();
    public FormatObj(Type type, SpStmt stmt, int index)
    {
        super(type, stmt);
        this.index = index;
    }
    public FormatObj(FormatObj parent, SootField field) {
        super(parent.getType(), parent.getStmt());
        this.index = parent.index;
        this.fields.addAll(parent.fields);
        this.fields.add(field);
    }
    public void setRealObj(Set<RealObj> objs) {
        realObjs.clear();
        realObjs.addAll(objs);
    }
    public Set<RealObj> getRealObjs() {
        return realObjs;
    }

    @Override
    public Type getType() {
        return type;
    }
    @Override
    public Obj castClone(SpStmt stmt, @Nullable Type newType) {
        if (newType == null)
            return this;
        FormatObj formatObj = new FormatObj(newType, stmt, index);
        formatObj.fields = fields;
        return formatObj;
    }
    public int getIndex() {
        return index;
    }
    public String toString() {
        return String.format("FormatObj: %d", index);
    }
    public List<SootField>  getFields() {
        return fields;
    }
    public InstanceField fieldPointer(SootField field) {
        FormatObj obj = new FormatObj(this, field);
        InstanceField instanceField = fieldPointer.computeIfAbsent(field, f -> new InstanceField(this, field));
        instanceField.add(obj);
        return instanceField;
    }
}
