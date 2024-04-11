package org.lambd.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class PrimeGenerator {
    private long currentPrime = 2;
    private Map<Object, Long> objectPrimeMap = new HashMap<>();
    private static PrimeGenerator generator;
    public static PrimeGenerator v() {
        if (generator == null) {
            generator = new PrimeGenerator();
        }
        return generator;
    }

    // 素数生成方法
    private long nextPrime() {
        outer: for (long num = currentPrime;; num++) {
            for (long i = 2; i * i <= num; i++) {
                if (num % i == 0) {
                    continue outer;
                }
            }
            currentPrime = num + 1;
            return num;
        }
    }

    // 为对象分配一个素数
    public long getPrime(Object object) {
        if (objectPrimeMap.containsKey(object))
            return objectPrimeMap.get(object);
        long prime = nextPrime();
        objectPrimeMap.put(object, prime);
        return prime;
    }
    // 素数分解
    public String express(long number) {
        List<Object> objects = new ArrayList<>();
        StringBuilder fields = new StringBuilder();
        for (Map.Entry<Object, Long> entry : objectPrimeMap.entrySet()) {
            if (number % entry.getValue() == 0) {
                objects.add(entry.getKey());
            }
        }
        // reverse fields
        for (int i = objects.size() - 1; i >= 0; i--)
            fields.append(".").append(objects.get(i));
        return fields.toString();
    }
}

