package org.lambd.transition;

import soot.SootField;

public record FieldTransition(int from, int to, SootField field, boolean direction) {
}