package org.lambd.obj;

import org.lambd.SpMethod;
import org.lambd.transition.Weight;
import soot.SootField;
import soot.Type;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class FormatObj extends Obj {
    private int index;
    private GenObj genObj;
    protected String fields = "";

    public FormatObj(Type type, SpMethod method, int index)
    {
        super(type, method);
        this.index = index;
    }
    public int getIndex() {
        return index;
    }
    public String toString() {
        return String.format("FormatObj: %s@%s", type, container.getName());
    }
//    public List<String> getFields() {
//        if (fields != null) {
//            return fields;
//        }
//        FormatObj tmpObj = this;
//        fields = new ArrayList<>();
//        while (tmpObj instanceof GenObj genObj) {
//            fields.add(genObj.getField());
//            tmpObj = genObj.getParent();
//        }
//        return fields;
//    }
    public String getFields() {
        return fields;
    }
    public boolean isFormat() {
        return true;
    }
}
