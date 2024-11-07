package org.lambd;

import java.util.List;
import java.util.Map;

import org.lambd.transition.Transition;
import org.lambd.transition.Weight;
import org.yaml.snakeyaml.Yaml;
import soot.jimple.Stmt;

import java.io.InputStream;

public class Config {
    public List<String> classPath;
    public Source source;
    public List<Sink> sinks;
    public List<Transfer> transfers;

    // Getters and Setters

    public static class Source {
        public String className;
        public String method;

        // Getters and Setters
    }

    public static class Sink {
        public String method;
        public int index;

        // Getters and Setters
    }

    public static class Transfer {
        public String method;
        public List<BaseTransition> transitions;

        // Getters and Setters
    }

    /**
     * 数据流配置：from-kind->to
     * kind = 1: real copy byte
     */
    public static class BaseTransition implements Transition {
        public int from;
        public int to;
        public int kind;
        public String toString() {
            return "(" + from + ", " + to + ", " + kind +")";
        }
        @Override
        public void apply(SpMethod method, Stmt stmt) {
            Weight relation = Weight.ONE;
            if (kind == 1)
                relation = Weight.COPY;
            method.handleTransition(stmt, from, to, relation);
        }
        // Getters and Setters
    }
}
