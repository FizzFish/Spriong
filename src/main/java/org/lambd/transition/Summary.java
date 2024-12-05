package org.lambd.transition;

import org.lambd.SootWorld;
import org.lambd.SpMethod;
import org.lambd.condition.Context;
import org.lambd.transformer.SpStmt;
import soot.Local;
import soot.RefType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 包含argtrans、sinktrans、rettrans三种摘要效果
 * updateCallers为了解决callgraph出现环的情况[TODO]
 */
public class Summary {
    private SpMethod container;
    private Map<Context, Effect> effectMap = new HashMap<>();
    public Summary(SpMethod container) {
        this.container = container;
    }
    public void addReturn(RefType type) {
        effectMap.get(container.getContext()).addReturn(type);
    }
    public void addTransition(int from, int to, Weight w, SpStmt stmt) {
        effectMap.get(container.getContext()).addTransition(from, to, w, stmt);
    }
    public void addSink(String sink, int index, Weight weight, SpStmt stmt) {
        effectMap.get(container.getContext()).addSink(sink, index, weight, stmt);
    }
    public void applyLastEffect(SpMethod caller, SpStmt stmt) {
        effectMap.get(container.getContext()).apply(caller, stmt);
    }
    public void print(boolean simple) {
        effectMap.values().forEach(e -> e.print(simple, container.getName()));
    }
    public boolean isEmpty() {
        return effectMap.isEmpty();
    }
    public Effect match(Map<Integer, Local> varMap) {
        for (Context context: effectMap.keySet()) {
            if (context.satisfy(varMap)) {
                return effectMap.get(context);
            }
        }
        return null;
    }

    public Map<String, Set<ArgTrans>> getTransition() {
        return effectMap.values().stream()
                .map(Effect::getTransition)  // 获取 Map<String, Set<ArgTrans>> 类型的 Transition
                .flatMap(map -> map.entrySet().stream())  // 展开 Map 中的每个条目 (String -> Set<ArgTrans>)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,  // 使用条目的键作为新 Map 的键
                        Map.Entry::getValue,  // 使用条目的值作为新 Map 的值
                        (set1, set2) -> {  // 合并 Set<ArgTrans>
                            Set<ArgTrans> mergedSet = new HashSet<>(set1);  // 创建一个新的 Set
                            mergedSet.addAll(set2);  // 合并两个 Set<ArgTrans>
                            return mergedSet;  // 返回合并后的 Set<ArgTrans>
                        }
                ));
    }
    public Map<String, Set<SinkTrans>> getSink() {
        return effectMap.values().stream()
                .map(Effect::getSink)  // 获取 Map<String, Set<ArgTrans>> 类型的 Transition
                .flatMap(map -> map.entrySet().stream())  // 展开 Map 中的每个条目 (String -> Set<ArgTrans>)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,  // 使用条目的键作为新 Map 的键
                        Map.Entry::getValue,  // 使用条目的值作为新 Map 的值
                        (set1, set2) -> {  // 合并 Set<ArgTrans>
                            Set<SinkTrans> mergedSet = new HashSet<>(set1);  // 创建一个新的 Set
                            mergedSet.addAll(set2);  // 合并两个 Set<ArgTrans>
                            return mergedSet;  // 返回合并后的 Set<ArgTrans>
                        }
                ));
    }
}
