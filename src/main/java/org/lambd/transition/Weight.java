package org.lambd.transition;

import org.apache.commons.math3.fraction.Fraction;

public class Weight {
    private Fraction fraction;
    private boolean update;
    public static final Weight ZERO = new Weight(Fraction.ZERO, false);
    public static final Weight ONE = new Weight(Fraction.ONE, true);
    public Weight(Fraction fraction, boolean update) {
        this.fraction = fraction;
        this.update = update;
    }
    public Weight(Fraction fraction) {
        this(fraction, true);
    }
    public Fraction getFraction() {
        return fraction;
    }
    public boolean isUpdate() {
        return update;
    }
    public boolean isZero() {
        return fraction.equals(Fraction.ZERO);
    }
    public Weight multiply(Weight other) {
        return new Weight(fraction.multiply(other.fraction), update && other.update);
    }
    public Weight divide(Weight other) {
        return new Weight(fraction.divide(other.fraction), update && other.update);
    }
    public int compareTo(Weight other) {
        return fraction.compareTo(other.fraction);
    }
    public String toString() {
        return fraction + (update ? "*" : "");
    }
    public static Weight max(Weight w1, Weight w2) {
        if (w1.compareTo(w2) >= 0)
            return w1;
        return w2;
    }
}
