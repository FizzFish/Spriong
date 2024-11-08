package org.lambd.anonotation;

import soot.tagkit.AnnotationStringElem;
import soot.tagkit.AnnotationTag;
import soot.tagkit.VisibilityAnnotationTag;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class Annotation {
    private final AnnotationType annotationType;
    private final Map<String, String> elements;

    public Annotation(String type, Map<String, String> elements) {
        this.annotationType = AnnotationType.fromType(type);
        this.elements = elements;
    }
    public String getAnnotationType() {
        return annotationType.getType();
    }

    public void apply() {
        if (annotationType != null) {
            annotationType.apply(elements);

        } else {
            // 处理未知的类型
            System.out.println("Unknown Annotation Type");
        }
    }
    public static Annotation extractAnnotation(AnnotationTag tag) {
        String type = tag.getType();
        System.out.println("Annotation Type: " + type);
        Map<String, String> elements = new HashMap<>();
        tag.getElems().forEach(e -> {
            String name = e.getName();
            if (e instanceof AnnotationStringElem se) {
                elements.put(name, se.getValue());
            }
        });
        return new Annotation(type, elements);
    }
}
