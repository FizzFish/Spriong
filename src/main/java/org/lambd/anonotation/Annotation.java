package org.lambd.anonotation;

import soot.tagkit.AnnotationStringElem;
import soot.tagkit.AnnotationTag;
import soot.tagkit.VisibilityAnnotationTag;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 *
 */
public class Annotation {
    private final AnnotationType annotationType;
    private final Map<String, String> elements;

    public Annotation(AnnotationType type, Map<String, String> elements) {
        this.annotationType = type;
        this.elements = elements;
    }
    public AnnotationType getAnnotationType() {
        return annotationType;
    }

    public Consumer apply() {
        return annotationType.apply();
    }
    public static Annotation extractAnnotation(AnnotationTag tag) {
        AnnotationType type = AnnotationType.fromType(tag.getType());
        if (type == null)
            return null;

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
