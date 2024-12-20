package org.lambd.annotation;

import org.lambd.SootWorld;
import org.lambd.SpMethod;
import org.lambd.wrapper.SpClass;
import org.lambd.wrapper.Wrapper;
import soot.SootClass;

import java.util.Map;

public enum AnnotationType {
    // 1. addbean 2. __init__ other bean
    COMPONENT("Lorg/springframework/stereotype/Component;", true) {
        @Override
        public void apply(Wrapper wrapper, Map<String, String> elements) {
            addBean((SpClass) wrapper);
        }
    },
    SERVICE("Lorg/springframework/stereotype/Service;", true) {
        @Override
        public void apply(Wrapper wrapper, Map<String, String> elements) {
            addBean((SpClass) wrapper);
        }
    },
    SpringBootApplication("Lorg/springframework/boot/autoconfigure/SpringBootApplication;", true) {
        @Override
        public void apply(Wrapper wrapper, Map<String, String> elements) {
            // 实现 Service 注解的特定行为
//            String packages = map.get("scanBasePackages");
        }
    },
    SHELLCOMPONENT("Lorg/springframework/shell/standard/ShellComponent;", true) {
        @Override
        public void apply(Wrapper wrapper, Map<String, String> elements) {
//            scanShellMethod((SpSootClass) wrapper);
        }
    },
    POST("Ljavax/ws/rs/POST;", false) {
        @Override
        public void apply(Wrapper wrapper, Map<String, String> elements) {
            addEntry((SpMethod) wrapper);
        }
    },
    GET("Ljavax/ws/rs/GET;", false) {
        @Override
        public void apply(Wrapper wrapper, Map<String, String> elements) {
            addEntry((SpMethod) wrapper);
        }
    },
    PUT("Ljavax/ws/rs/PUT;", false) {
        @Override
        public void apply(Wrapper wrapper, Map<String, String> elements) {
            addEntry((SpMethod) wrapper);
        }
    },
    PATH("Ljavax/ws/rs/Path;", false) {
    },
    AUTOWIRED("Lorg.springframework.beans.factory.annotation.Autowired;", false) {
    },
    SHELLMETHOD("Lorg/springframework/shell/standard/ShellMethod;", false) {
        @Override
        public void apply(Wrapper wrapper, Map<String, String> elements) {
            addEntry((SpMethod) wrapper);
        }
    },
    RPCMETHOD("Lio/grpc/stub/annotations/RpcMethod;", false) {
        @Override
        public void apply(Wrapper wrapper, Map<String, String> elements) {
            SootClass sc = ((SpMethod) wrapper).getSootMethod().getDeclaringClass();
            String clsWithMethod = elements.get("fullMethodName");
            String methodName = clsWithMethod.substring(clsWithMethod.lastIndexOf("/") + 1);
            SootWorld.v().getAutoWired().addService(sc, methodName);
        }
    };

    private final String type;
    private final boolean classOrMethod;
    private static void addBean(SpClass sc) {
        AutoWired aw = SootWorld.v().getAutoWired();
        aw.addBean(sc.getSootClass().getType(), sc);
        aw.wired(sc);
    }
    private static void scanShellMethod(SpClass ssc) {
        AutoWired aw = SootWorld.v().getAutoWired();
        aw.scanShellMethod(ssc);
    }
    private static void addEntry(SpMethod sm) {

        SootWorld.v().addEntryPoint(sm.getSootMethod());
    }
    AnnotationType(String type, boolean classOrMethod) {
        this.type = type;
        this.classOrMethod = classOrMethod;
    }

    public void apply(Wrapper wrapper, Map<String, String> elements) {
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

