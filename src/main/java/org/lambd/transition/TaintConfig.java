package org.lambd.transition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.math3.fraction.Fraction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TaintConfig {
    private String configFile;

    public TaintConfig(String configFile) {
        this.configFile = configFile;
    }
    public String parse(Map<String, List<Transition>> methodRefMap) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        String source = null;
        try {
            Knowledge knowledge = mapper.readValue(new File(configFile), Knowledge.class);
            source = knowledge.source();
            knowledge.transfers().forEach(transfer -> {
                methodRefMap.put(transfer.method(),
                        transfer.transitions().stream().
                                map(t -> (Transition) t).collect(Collectors.toList()));
            });
            knowledge.sinks().forEach(sink -> {
                List<Transition> sinkList = new ArrayList<>();
                sinkList.add(new SinkTransition(sink.index(), Fraction.ONE, sink.method()));
                methodRefMap.put(sink.method(), sinkList);
            });
            // 输出结果，验证是否正确解析
        } catch (Exception e) {
            e.printStackTrace();
        }
        return source;
    }
}
record Knowledge(String source, List<Sink> sinks, List<Transfer> transfers) {
}
record Sink(String method, int index) {
    public String toString() {
        return method + ": " + index;
    }
}

record Transfer(String method, List<BaseTransition> transitions) {
    public String toString() {
        return method + ": " + transitions;
    }
}
