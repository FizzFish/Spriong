package org.lambd.transition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.util.List;
import java.util.Map;

public class TaintConfig {
    private String configFile;

    public TaintConfig(String configFile) {
        this.configFile = configFile;
    }
    public void parse(Map<String, List<BaseTransition>> transferMap, Map<String, Integer> sinkIndex) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            Knowledge knowledge = mapper.readValue(new File(configFile), Knowledge.class);
            System.out.println(knowledge);
            knowledge.transfers().forEach(transfer -> {
                transferMap.put(transfer.method(), transfer.transitions());
            });
            knowledge.sinks().forEach(sink -> {
                sinkIndex.put(sink.method(), sink.index());
            });
            // 输出结果，验证是否正确解析
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
record Knowledge(List<Sink> sinks, List<Transfer> transfers) {
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
