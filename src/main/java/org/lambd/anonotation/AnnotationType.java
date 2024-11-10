package org.lambd.anonotation;

import org.lambd.SootWorld;
import soot.SootClass;
import soot.SootMethod;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum AnnotationType {
    // 1. addbean 2. __init__ other bean
    COMPONENT("Lorg.springframework.stereotype.Component;", true) {
        @Override
        public Consumer apply() {
            // 实现 Component 注解的特定行为
            return addBean.andThen(initMethod);
        }
    },
    SERVICE("Lorg.springframework.stereotype.Service;", true) {
        @Override
        public Consumer apply() {
            // 实现 Component 注解的特定行为
            return addBean.andThen(initMethod);
        }
    },
    SpringBootApplication("Lorg/springframework/boot/autoconfigure/SpringBootApplication;", true) {
        @Override
        public Consumer apply() {
            // 实现 Service 注解的特定行为
//            String packages = map.get("scanBasePackages");
            return nothing;
        }
    },
    SHELLCOMPONENT("Lorg/springframework/shell/standard/ShellComponent;", true) {
        @Override
        public Consumer apply() {
            // 实现 Component 注解的特定行为
            return nothing;
        }
    },
    POST("Ljavax/ws/rs/POST;", false) {
        @Override
        public Consumer apply() {
            // 实现 Component 注解的特定行为
            return entry;
        }
    },
    PATH("Ljavax/ws/rs/Path;", false) {
        @Override
        public Consumer apply() {
            // 实现 Component 注解的特定行为
            return nothing;
        }
    },
    AUTOWIRED("Lorg.springframework.beans.factory.annotation.Autowired;", false) {
        @Override
        public Consumer apply() {
            // 实现 Component 注解的特定行为
            return entry;
        }
    },
    SHELLMETHOD("Lorg/springframework/shell/standard/ShellMethod;", false) {
        @Override
        public Consumer apply() {
            // 实现 Component 注解的特定行为
            return entry;
        }
    };

    private final String type;
    private final boolean classOrMethod;
    private static final Consumer<SootClass> addBean = sc -> {
        SootWorld.v().getAutoWired().addBean(sc.getType(), sc);
    };
    private static final Consumer<SootClass> initMethod = sc -> {
        SootWorld.v().getAutoWired().wired(sc);
    };
    private static final Consumer<SootMethod> entry = sm -> {
        SootWorld.v().visitMethod(sm);
    };
    private static final Consumer nothing = s -> {};
    AnnotationType(String type, boolean classOrMethod) {
        this.type = type;
        this.classOrMethod = classOrMethod;
    }

    public Consumer apply() {
        return nothing;
    };

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

