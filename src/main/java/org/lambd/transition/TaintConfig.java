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
    public void parse(Map<String, List<Transition>> methodRefMap, List<String> sourceInfo) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            Knowledge knowledge = mapper.readValue(new File(configFile), Knowledge.class);
            Source source = knowledge.source();
            sourceInfo.add(source.className());
            sourceInfo.add(source.method());
            knowledge.transfers().forEach(transfer -> {
                methodRefMap.put(transfer.method(),
                        transfer.transitions().stream().
                                map(t -> (Transition) t).collect(Collectors.toList()));
            });
            knowledge.sinks().forEach(sink -> {
                List<Transition> sinkList = new ArrayList<>();
                sinkList.add(new SinkTrans(sink.index(), Weight.ONE, sink.method()));
                methodRefMap.put(sink.method(), sinkList);
            });
            // 输出结果，验证是否正确解析
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
record Knowledge(Source source, List<Sink> sinks, List<Transfer> transfers) {
}
record Source(String className, String method) {
}
record Sink(String method, int index) {
    public String toString() {
        return method + ": " + index;
    }
}

record Transfer(String method, List<BaseTrans> transitions) {
    public String toString() {
        return method + ": " + transitions;
    }
}
