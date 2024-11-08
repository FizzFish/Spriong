package org.lambd;

import java.util.Map;

public class Annotion {
    private final String annotationType;
    private final Map<String, String> elements;
    public Annotion(String annotationType, Map<String, String> elements) {
        this.annotationType = annotationType;
        this.elements = elements;
    }
    public String getAnnotationType() {
        return annotationType;
    }
}
