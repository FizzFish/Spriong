package org.lambd.obj;

public class Relation {
    private Fraction fraction;
    private boolean update;
    public static final Relation Identity = new Relation(Fraction.one(), false);
    public Relation(Fraction fraction, boolean update) {
        this.fraction = fraction;
        this.update = update;
    }
    public Fraction getFraction() {
        return fraction;
    }
    public boolean isUpdate() {
        return update;
    }
    public String toString() {
        return fraction.details() + (update ? "*" : "");
    }
}
