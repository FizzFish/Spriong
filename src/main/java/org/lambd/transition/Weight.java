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
    private String srcFields = "";
    private String dstFields = "";
    private boolean update = false;
    public Weight(String fields, String _fields) {
        this.srcFields = fields;
        this.dstFields = _fields;
    }
    public Weight(String field) {
        this.srcFields = field;
    }
    public Weight(boolean update) {
        this.update = update;
    }
    public Weight(String fields, Weight w) {
        this.srcFields = Utils.concat(fields, w.srcFields);
        this.dstFields = w.dstFields;
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
            base = "-" + dstFields;
        else if (dstFields.isEmpty())
            base = srcFields;
        else
            base = srcFields + "-" + dstFields;
        return base + (update ? "*" : "");
    }
    public boolean equals(Object obj) {
        if (obj instanceof Weight w) {
            return srcFields.equals(w.srcFields) && dstFields.equals(w.dstFields);
        }
        return false;
    }
    public int hashCode() {
        return srcFields.hashCode() + dstFields.hashCode();
    }
    public List<String> getFromFields() {
        if (srcFields.isEmpty())
            return Collections.emptyList();
        return Arrays.stream(srcFields.split("\\.")).collect(Collectors.toList());
    }
    public List<String> getToFields() {
        if (dstFields.isEmpty())
            return Collections.emptyList();
        return Arrays.stream(dstFields.split("\\.")).collect(Collectors.toList());
    }

}
