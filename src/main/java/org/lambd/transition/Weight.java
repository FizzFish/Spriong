package org.lambd.transition;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * p.f1 = arg0.f0 => w(arg0, p) = f0 / f1
 * then p.f2 is unrelated with arg0, because f2 != f1
 */
public class Weight {
    private int update = ASSIGN;
    public static final int ASSIGN = 0;
    public static final int EFFECT = 1;
    public static final int BYTES = 2;
    private Map<String, Integer> fieldCount;
    public static final Weight ONE = new Weight(ASSIGN);
    // x = new Obj() or param/this
    public static final Weight ID = new Weight(EFFECT);
    public static final Weight COPY = new Weight(BYTES);
    // ONE
    public Weight(int update) {
        this.fieldCount = new HashMap<>();
        this.update = update;
    }

    public Weight(String field, int count) {
        this.fieldCount = new HashMap<>();
        fieldCount.put(field, count);
    }

//    public Weight(Map<String, Integer> fieldCount) {
//        this.fieldCount = fieldCount;
//    }
    public void addField(String field, int count) {
        this.fieldCount.merge(field, count, (oldValue, value) -> {
            int newCount = oldValue + value;
            return newCount != 0 ? newCount : null;
        });
    }
    public void setUpdate(int update) {
        this.update = update;
    }
    public boolean hasEffect() {
        return update >= EFFECT;
    }
    public boolean isUpdate() {
        return update == BYTES;
    }
    public Weight multiply(Weight other) {
        // keep this.update
        Weight result = new Weight(this.update);
        // 添加当前对象的字段
        this.fieldCount.forEach(result::addField);
        // 添加另一个对象的字段并应用乘法逻辑
        other.fieldCount.forEach(result::addField);
        return result;
    }
    public Weight divide(Weight other) {
        int update = this.update > other.update ? this.update : other.update;
        Weight result = new Weight(update);
        // 添加当前对象的字段
        this.fieldCount.forEach(result::addField);
        // 添加另一个对象的字段并应用乘法逻辑
        other.fieldCount.forEach((field, count) -> result.addField(field, -count));
        return result;
    }
    public int compareTo(Weight other) {
        // 倾向保留简洁的形式
        if (fieldCount.isEmpty() || other.fieldCount.isEmpty())
            return 1;
        return fieldCount.size() - other.fieldCount.size();
    }
    public String toString() {
        if (this == ONE)
            return "1";
        if (this == COPY)
            return "copy*";
        return fieldCount + (update == 2 ? "*" : "");
    }
    public boolean hasOverField() {
        return fieldCount.entrySet().stream()
                .anyMatch(entry -> entry.getValue() < 0);
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
    /**
     * transition: y = x.w, x=argi.w0, y=argj.w1
     * argj = argi.w0.w/w1 = argj.(w0p.wp.w1n)/(w0n.wn.w1p)
     * w0n == wp && w1n > wn
     * eg. y.w1n=argj.w1p & y.wn=x.wp, then we need w1n include wn
     */
    public static boolean checkStore(Weight w0, Weight w1, Weight w) {
        Optional<String> w0n = w0.fieldCount.entrySet().stream().filter(entry -> entry.getValue() < 0).map(Map.Entry::getKey).findFirst();
        Optional<String> wp = w.fieldCount.entrySet().stream().filter(entry -> entry.getValue() > 0).map(Map.Entry::getKey).findFirst();
        boolean check1 = true;
        if (w0n.isPresent() && wp.isPresent() && w0n.get().equals(wp.get()))
            check1 = false;
        if (!check1)
            return false;
        Optional<String> w1n = w1.fieldCount.entrySet().stream().filter(entry -> entry.getValue() < 0).map(Map.Entry::getKey).findFirst();
        Optional<String> wn = w.fieldCount.entrySet().stream().filter(entry -> entry.getValue() < 0).map(Map.Entry::getKey).findFirst();
        if (w1n.isEmpty() && wn.isPresent())
            return true;
//        return wn.isPresent() && w1n.get().equals(wn.get());
        return false;
    }
    /**
     * load: y=x.w, x=argi.w0
     * y = argi.w0.w
     * w0n == wp
     */
    public static boolean checkLoad(Weight w0, Weight w) {
        Optional<String> w0n = w0.fieldCount.entrySet().stream().filter(entry -> entry.getValue() < 0).map(Map.Entry::getKey).findFirst();
        Optional<String> wp = w.fieldCount.entrySet().stream().filter(entry -> entry.getValue() > 0).map(Map.Entry::getKey).findFirst();
        if (w0n.isEmpty() || wp.isEmpty())
            return true;
        return w0n.get().equals(wp.get());
    }
    public static boolean include(Weight w0, Weight w1) {
        return w0.fieldCount.entrySet().stream().allMatch(entry -> {
            String field = entry.getKey();
            int count = entry.getValue();
            return w1.fieldCount.containsKey(field) && w1.fieldCount.get(field) >= count;
        });
    }
}
