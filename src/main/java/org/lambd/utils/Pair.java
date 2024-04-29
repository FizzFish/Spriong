package org.lambd.utils;

public class Pair {
    private int from;
    private int to;
    public Pair(int from, int to) {
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
        return from * 31 + to;
    }
}
