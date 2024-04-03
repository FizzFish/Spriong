package org.lambd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SootWorld {
    private static final Logger logger = LogManager.getLogger(SootWorld.class);
    public SootWorld() {}
    public void loadJar(String jarPath) {
        G.reset();
        String sootCp = String.format("src/main/resources/rt.jar;src/main/resources/%s", jarPath);
        Options.v().set_soot_classpath(sootCp);
        System.out.println(sootCp);
        String entryClassName = "org.apache.commons.text.StringSubstitutor";
        String entryMethodName = "java.lang.String replace(java.lang.String)";
        // 配置Soot
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_exclude(Arrays.asList("java.*", "javax.*", "sun.*", "jdk.*", "com.sun.*"));

        // 如果没有明确的入口点，可以考虑使用Scene.v().setEntryPoints()来设置应用的入口点；
        // 这里我们手动指定入口点
        SootClass entryClass = Scene.v().loadClassAndSupport(entryClassName);
        Scene.v().loadNecessaryClasses();
        entryClass.setApplicationClass();

        // 获取具有完整签名的方法
        // 注意替换"methodName"和方法签名以匹配你的实际情况
        SootMethod entryMethod = entryClass.getMethod(entryMethodName);
        List<SootMethod> entryPoints = new ArrayList<>();
        entryPoints.add(entryMethod);
        Scene.v().setEntryPoints(entryPoints);

        // 运行Soot分析
        PackManager.v().runPacks();

        // 获取并处理调用图
        CallGraph cg = Scene.v().getCallGraph();
//        System.out.printf("Call Graph: %s\n", cg.toString());
    }
}
