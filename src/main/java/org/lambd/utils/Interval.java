package org.lambd.utils;

public class Interval<T> {
    private T low;
    private T high;
    public Interval(T low, T high) {
        this.low = low;
        this.high = high;
    }
    public T getLow() { return low; }
    public T getHigh() { return high; }
    public boolean in() {
        return false;
    }
}
