package org.lambd;

import java.util.List;
import java.util.Map;

import org.lambd.transition.BaseTransition;
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
        public List<Object> transitions;

        // Getters and Setters
    }
}
