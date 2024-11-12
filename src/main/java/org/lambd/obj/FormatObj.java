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
 * 当函数调用时，base.invoke(args...)时会生成多个参数对象，编号为-1,0,1...
 */
public class FormatObj extends Obj {
    private int index;
    protected List<SootField> fields = new ArrayList<>();

    public FormatObj(Type type, Stmt stmt, int index)
    {
        super(type, stmt);
        this.index = index;
    }
    public Obj castClone(Stmt stmt, @Nullable Type newType) {
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
    public boolean isFormat() {
        return true;
    }
}
