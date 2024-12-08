package org.lambd.obj;

import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lambd.condition.Condition;
import org.lambd.pointer.InstanceField;
import org.lambd.transformer.SpStmt;
import org.lambd.utils.Utils;
import soot.SootField;
import soot.Type;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * FormatObj代表一个参数抽象对象
 * 在一个context调用下，他会关联一个实际的Obj
 * type可能是一个不确定的类型，因此在formatObj.invoke时，可能需要判断它的子类
 * 但是当cast(formatObj)或transfer后，可以认为他的类型是确定的了
 * 当函数调用时，base.invoke(args...)时会生成多个参数对象，编号为-1,0,1...
 */
public class FormatObj extends Obj implements Index {
    private static final Logger logger = LogManager.getLogger(FormatObj.class);
    private int index;
    // 在一个上下文中，应该只有一个实际对象
    private Set<RealObj> realObjs = new HashSet<>();
    protected List<SootField> fields = new ArrayList<>();
    private boolean resolved = false;
    private final FormatObj orignal;
    public FormatObj(Type type, SpStmt stmt, int index)
    {
        super(type, stmt);
        this.index = index;
        orignal = this;
    }
    // child.realObjs follow ancestor.realObjs
    public FormatObj(FormatObj parent, SootField field) {
        super(field.getType(), parent.getStmt());
        this.index = parent.index;
//        resolve(parent.realObjs, field);
        this.fields.addAll(parent.fields);
        this.fields.add(field);
        orignal = parent.orignal;
    }
    public void setRealObj(Set<RealObj> objs) {
        realObjs.clear();
        realObjs.addAll(objs);
        resolved = true;
    }
    private void resolve() {
//        realObjs = objs.stream().map(o -> o.fieldPointer(field))
//                    .flatMap(InstanceField::realObjs).collect(Collectors.toSet());
        Set<RealObj> objs = orignal.realObjs;
        for (SootField field : fields) {
            objs = objs.stream().map(o -> o.fieldPointer(field))
                    .flatMap(InstanceField::realObjs).collect(Collectors.toSet());
            if (objs.isEmpty())
                break;
        }
        realObjs = objs;
    }
    public Set<RealObj> getRealObjs() {
        if (!resolved)
            resolve();
        if (realObjs.isEmpty()) {
            if (fields.isEmpty())
                logger.info("FormatObj[{}:{}] has no realObj", getContainer().getName(), index);
            else
                logger.info("FormatObj[{}:{}] resolve filed {} failed", getContainer().getName(), index, Utils.fieldString(fields));
            realObjs.add(new GenObj(type, stmt));
        }
        return realObjs;
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

    @Override
    public boolean real() {
        return !realObjs.isEmpty();
    }
}
