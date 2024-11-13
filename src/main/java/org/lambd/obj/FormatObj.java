package org.lambd.obj;

import com.google.common.collect.Maps;
import org.lambd.SpMethod;
import org.lambd.transition.Weight;
import soot.RefType;
import soot.SootField;
import soot.Type;
import soot.jimple.Stmt;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * FormatObj代表一个参数抽象对象，type是声明的类型
 * type可能是一个不确定的类型，因此在formatObj.invoke时，可能需要判断它的子类
 * 但是当cast(formatObj)或transfer后，可以认为他的类型是确定的了
 * 当函数调用时，base.invoke(args...)时会生成多个参数对象，编号为-1,0,1...
 */
public class FormatObj implements Obj, Index {
    private int index;
    protected List<SootField> fields = new ArrayList<>();
    private Type type;
    private Stmt stmt;
    private boolean mayMultiple;
    public FormatObj(Type type, Stmt stmt, int index)
    {
        this.type = type;
        this.stmt = stmt;
        this.index = index;
        mayMultiple = true;
    }
    public FormatObj(FormatObj parent, SootField field, Stmt stmt) {
        this.type = field.getType();
        this.stmt = stmt;
        this.index = parent.index;
        this.fields.addAll(parent.fields);
        this.fields.add(field);
        mayMultiple = true;
    }

    @Override
    public Type getType() {
        return type;
    }
    @Override
    public Obj castClone(Stmt stmt, @Nullable Type newType) {
        if (newType == null)
            return this;
        FormatObj formatObj = new FormatObj(newType, stmt, index);
        formatObj.fields = fields;
        formatObj.mayMultiple = false;
        return formatObj;
    }
    public boolean isMayMultiple() {
        return mayMultiple;
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
}
