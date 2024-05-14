package org.lambd.obj;

import com.google.common.collect.Maps;
import org.lambd.SpMethod;
import org.lambd.transition.Weight;
import soot.RefType;
import soot.SootField;
import soot.Type;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FormatObj extends Obj {
    private int index;
    protected String fields = "";
    private static final Map<String, String> map = Map.of("org.apache.logging.log4j.core.LogEvent", "org.apache.logging.log4j.core.impl.MutableLogEvent",
            "org.apache.logging.log4j.spi.AbstractLogger", "org.apache.logging.log4j.core.Logger",
            "org.apache.logging.log4j.core.pattern.LogEventPatternConverter", "org.apache.logging.log4j.core.pattern.MessagePatternConverter",
            "org.apache.logging.log4j.message.ReusableMessage", "org.apache.logging.log4j.message.ReusableSimpleMessage",
            "org.apache.logging.log4j.message.Message", "org.apache.logging.log4j.message.ReusableSimpleMessage");

    public FormatObj(Type type, SpMethod method, int index)
    {
        super(type, method);
        if (type instanceof RefType rf) {
            if (map.containsKey(rf.getClassName()))
                this.type = RefType.v(map.get(rf.getClassName()));
        }
        this.index = index;
    }
    public int getIndex() {
        return index;
    }
    public String toString() {
        return String.format("FormatObj: %s@%s", type, container.getName());
    }
    public String getFields() {
        return fields;
    }
    public boolean isFormat() {
        return true;
    }
}
