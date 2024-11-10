package org.lambd.anonotation;

import org.lambd.SootWorld;
import org.lambd.SpMethod;
import org.lambd.wrapper.SpSootClass;
import org.lambd.wrapper.Wrapper;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.tagkit.AnnotationStringElem;
import soot.tagkit.AnnotationTag;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public void apply(Wrapper wrapper) {
        annotationType.apply(wrapper, elements);
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

    public static boolean hasAnnotation(SootMethod sm, AnnotationType type) {
        VisibilityAnnotationTag tag = (VisibilityAnnotationTag) sm.getTag("VisibilityAnnotationTag");
        if (tag == null)
            return false;
        for (AnnotationTag anno : tag.getAnnotations())
            if (anno.getType().equals(type.getType()))
                return true;
        return false;
    }
}
