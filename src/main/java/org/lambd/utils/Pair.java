package org.lambd.utils;

import java.util.Objects;

public class Pair<T> {
    private T from;
    private T to;
    public Pair(T from, T to) {
        this.from = from;
        this.to = to;
    }
    public boolean equals(Object o) {
        if (o instanceof Pair) {
            Pair p = (Pair) o;
            return p.from == from && p.to == to;
        }
        return false;
    }
    public int hashCode() {
        return Objects.hash(from, to);
    }
}
