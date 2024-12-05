package org.lambd.condition;

/**
 * 代表程序中的条件，理论上每个Statement都处于一个条件中
 * 条件能够组成一棵树 ConditionTree
 * 如果两个Obj是isBrother的状态，则应该使用强更新StrongUpdate
 */
public class Condition {
    public static Condition ROOT = new Condition("root");
    private String repr;
    private Condition parent;
    public Condition(String repr) {
        this.repr = repr;
        parent = ROOT;
    }
    public Condition(String repr, Condition parent) {
        this.repr = repr;
        this.parent = parent;
    }
    public String getRepr() {
        return repr;
    }
    public Condition getParent() {
        return parent;
    }
    public boolean isBrother(Condition other) {
        return this.parent.equals(other.parent);
    }
    public Condition not() {
        return new Condition("!" + repr, this.parent);
    }
}
