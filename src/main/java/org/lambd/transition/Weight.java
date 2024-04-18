package org.lambd.transition;

import org.apache.commons.math3.fraction.Fraction;
import org.lambd.utils.PrimeGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Weight {
    private boolean update;
    private int num;
    private Map<String, Integer> fieldCount;
    public static final Weight ZERO = new Weight(false);
    public static final Weight ONE = new Weight();
    // x = new Obj() or param/this
    public static final Weight ID = new Weight(true);
    public static final Weight COPY = new Weight(true);
    // ONE
    public Weight() {
        this.fieldCount = new HashMap<>();
        this.update = false;
        num = 1;
    }
    public Weight(boolean update) {
        this();
        this.update = update;
        if (!update)
            num = 0;
    }

    public Weight(String field, int count) {
        this();
        fieldCount.put(field, count);
    }

    public Weight(Map<String, Integer> fieldCount) {
        this.fieldCount = fieldCount;
    }
    public void addField(String field, int count) {
        this.fieldCount.merge(field, count, (oldValue, value) -> {
            int newCount = oldValue + value;
            return newCount != 0 ? newCount : null;
        });
    }
    public void setUpdate(boolean update) {
        this.update = update;
    }
    public boolean isUpdate() {
        return update;
    }
    public boolean isZero() {
        return this == ZERO;
    }
    public Weight multiply(Weight other) {
        if (this == ZERO || other == ZERO)
            return ZERO;
        Weight result = new Weight();
        // 添加当前对象的字段
        this.fieldCount.forEach(result::addField);
        // 添加另一个对象的字段并应用乘法逻辑
        other.fieldCount.forEach(result::addField);
        return result;
    }
    public Weight divide(Weight other) {
        if (this == ZERO)
            return ZERO;
        Weight result = new Weight();
        // 添加当前对象的字段
        this.fieldCount.forEach(result::addField);
        // 添加另一个对象的字段并应用乘法逻辑
        other.fieldCount.forEach((field, count) -> result.addField(field, -count));
        return result;
    }
    public int compareTo(Weight other) {
        // 倾向保留简洁的形式
        if (fieldCount.isEmpty() || other.fieldCount.isEmpty())
            return num - other.num;
        return other.fieldCount.size() - fieldCount.size();
    }
    public String toString() {
        if (this == ZERO)
            return "0";
        if (this == ONE)
            return "1";
        if (this == COPY)
            return "copy*";
        return fieldCount + (update ? "*" : "");
    }
    public String getPositive() {
        return fieldCount.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> entry.getKey() + (entry.getValue() > 1 ? "*" : "")) // 格式化输出
                .collect(Collectors.joining(",", "{", "}"));
    }
    public String getNegative() {
        return fieldCount.entrySet().stream()
                .filter(entry -> entry.getValue() < 0)
                .map(entry -> entry.getKey() + (entry.getValue() < -1 ? "*" : "")) // 格式化输出
                .collect(Collectors.joining(",", "{", "}"));
    }
    public static Weight max(Weight w1, Weight w2) {
        if (w1.compareTo(w2) >= 0)
            return w1;
        return w2;
    }
}
