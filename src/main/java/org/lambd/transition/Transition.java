package org.lambd.transition;

public interface Transition {
    default void toMatrix() {}
    default void fromMatrix() {}
}
