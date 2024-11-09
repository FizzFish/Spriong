package org.lambd.anonotation;

import java.util.Map;

public enum AnnotationType {
    COMPONENT("Lorg.springframework.stereotype.Component;") {
        @Override
        public void apply(Map<String, String> map) {
            // 实现 Component 注解的特定行为
            System.out.println("Applying Component Annotation");
        }
    },
    SHELLCOMPONENT("Lorg/springframework/shell/standard/ShellComponent;") {
        @Override
        public void apply(Map<String, String> map) {
            // 实现 Component 注解的特定行为
            System.out.println("Applying Shell Component Annotation");
        }
    },
    SERVICE("Lorg.springframework.stereotype.Service;") {
        @Override
        public void apply(Map<String, String> map) {
            // 实现 Service 注解的特定行为
            System.out.println("Applying Service Annotation");
        }
    },
    //Lorg/springframework/boot/autoconfigure/SpringBootApplication;
    SpringBootApplication("Lorg/springframework/boot/autoconfigure/SpringBootApplication;") {
        @Override
        public void apply(Map<String, String> map) {
            // 实现 Service 注解的特定行为
            System.out.println("Applying SpringBootApplication Annotation");
        }
    },
    POST("Ljavax/ws/rs/POST;") {
        @Override
        public void apply(Map<String, String> map) {
            // 实现 Service 注解的特定行为
            System.out.println("Applying SpringBootApplication Annotation");
        }
    },
    PATH("Ljavax/ws/rs/Path;") {
        @Override
        public void apply(Map<String, String> map) {
            // 实现 Service 注解的特定行为
            System.out.println("Applying SpringBootApplication Annotation");
        }
    },
    SHELLMETHOD("Lorg/springframework/shell/standard/ShellMethod;") {
        @Override
        public void apply(Map<String, String> map) {
            // 实现 Service 注解的特定行为
            System.out.println("Applying SpringBootApplication Annotation");
        }
    };

    private final String type;

    AnnotationType(String type) {
        this.type = type;
    }

    public abstract void apply(Map<String, String> map);

    public String getType() {
        return type;
    }

    public static AnnotationType fromType(String type) {
        for (AnnotationType annotationType : values()) {
            if (annotationType.type.equals(type)) {
                return annotationType;
            }
        }
        return null;
    }
}

