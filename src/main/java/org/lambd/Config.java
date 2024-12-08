package org.lambd;

import java.util.List;
import java.util.Map;

import org.lambd.transition.BaseTransition;
import org.lambd.transition.Transition;
import org.lambd.transition.Weight;
import org.yaml.snakeyaml.Yaml;
import soot.jimple.Stmt;

import java.io.InputStream;
import java.util.Objects;

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
        public boolean equals(Object other) {
            if (other instanceof Transfer transfer)
                return method.equals(transfer.method);
            return false;
        }
        public int hashCode() {
        return Objects.hash(method, transitions);
    }
        // Getters and Setters
    }
    public void merge(Config overrideConfig) {
        this.classPath = overrideConfig.classPath;
        this.source = overrideConfig.source;
        this.sinks = overrideConfig.sinks;
        for (Transfer transfer: overrideConfig.transfers)
            if (!this.transfers.contains(transfer))
                this.transfers.add(transfer);
    }
}
