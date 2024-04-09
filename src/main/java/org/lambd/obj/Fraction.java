package org.lambd.obj;

import org.lambd.utils.PrimeGenerator;

public class Fraction {
    static final Fraction one = new Fraction(1, 1);
    long numerator;
    long denominator;

    public Fraction(long a, long b) {
        numerator = a;
        denominator = b;
    }
    public static Fraction one() {
        return one;
    }

    public void multiply(Fraction other) {
        // need gcd
        push(other.numerator);
        pop(other.denominator);
    }
    public void divide(Fraction other) {
        pop(other.numerator);
        push(other.denominator);
    }
    public void push(long num) {
        if (denominator % num == 0)
            denominator /= num;
        else
            numerator *= num;
    }
    public void pop(long num) {
        if (numerator % num == 0)
            numerator /= num;
        else
            denominator *= num;
    }
    public Fraction copy() {
        return new Fraction(numerator, denominator);
    }
    public String toString() {
        return numerator + "/" + denominator;
    }
    public String details() {
        PrimeGenerator generator = PrimeGenerator.v();
        return String.format("%s/%s", generator.primeFactorization(numerator), generator.primeFactorization(denominator));
    }
    public static Fraction multiply(Fraction f1, Fraction f2) {
        long numerator = f1.numerator * f2.numerator;
        long denominator = f1.denominator * f2.denominator;
        long gcd = gcd(numerator, denominator); // 计算最大公约数
        numerator /= gcd;
        denominator /= gcd;
        return new Fraction(numerator, denominator);
    }

    // 计算最大公约数的辅助方法（欧几里得算法）
    private static long gcd(long a, long b) {
        return b == 0 ? a : gcd(b, a % b);
    }
}
