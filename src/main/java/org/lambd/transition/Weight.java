package org.lambd.transition;

import org.lambd.utils.Utils;
import soot.SootField;

import java.util.*;
import java.util.stream.Collectors;

/**
 * p.f1 = arg0.f0 => w(arg0, p) = f0 / f1
 * then p.f2 is unrelated with arg0, because f2 != f1
 */
public class Weight {
    public static final Weight COPY = new Weight(true);
    public static final Weight ONE = new Weight(false);
    private List<SootField> srcFields = new ArrayList<>();
    private List<SootField> dstFields = new ArrayList<>();
    private boolean update = false;
    public Weight(List<SootField> srcFields, List<SootField> dstFields) {
        this.srcFields.addAll(srcFields);
        this.dstFields.addAll(dstFields);
    }
    public Weight(List<SootField> srcFields, SootField dstField) {
        this.srcFields.addAll(srcFields);
        this.dstFields.add(dstField);
    }
    public Weight(List<SootField> field) {
        this.srcFields.addAll(field);
    }
    public Weight(boolean update) {
        this.update = update;
    }
    public Weight(SootField fields, Weight w) {
        this.srcFields.add(fields);
        this.srcFields.addAll(w.srcFields);
        this.dstFields.addAll(w.dstFields);
        this.update = w.update;
    }
    public boolean isUpdate() {
        return update;
    }
    public void setUpdate() {
        update = true;
    }
    public String toString() {
        String base;
        if (srcFields.isEmpty() && dstFields.isEmpty())
            base = "one";
        else if (srcFields.isEmpty())
            base = "-" + Utils.fieldString(dstFields);
        else if (dstFields.isEmpty())
            base = Utils.fieldString(srcFields);
        else
            base = Utils.fieldString(srcFields) + "-" + Utils.fieldString(dstFields);
        return base + (update ? "*" : "");
    }
    public boolean equals(Object obj) {
        if (obj instanceof Weight w) {
            return toString().equals(w.toString());
        }
        return false;
    }
    public int hashCode() {
        return Objects.hash(srcFields, dstFields);
    }
    public List<SootField> getFromFields() {
        return srcFields;
    }
    public List<SootField> getToFields() {
        return dstFields;
    }

}
