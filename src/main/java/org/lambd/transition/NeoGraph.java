package org.lambd.transition;

import org.lambd.SpMethod;
import org.lambd.utils.Pair;
import org.neo4j.driver.*;
import soot.SootMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.neo4j.driver.Values.parameters;

public class NeoGraph implements AutoCloseable {
    private Driver driver = null;
    private String database = "neo4j";
    private final List<Map<String, String>> methodsToUpdate = new ArrayList<>();
    private final List<Map<String, String>> sinksToUpdate = new ArrayList<>();
    private final List<Map<String, String>> sourcesToUpdate = new ArrayList<>();
    private final List<Map<String, String>> relationshipsToUpdate = new ArrayList<>();
    private List<String> visited = new ArrayList<>();
    private List<Pair<String>> visitedEdge = new ArrayList<>();
    private boolean save;
    public NeoGraph(String uri, String user, String password, String database, boolean save) {
        this.database = database;
        this.save = save;
        if (save)
            driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    @Override
    public void close() throws RuntimeException {
        if (save)
            driver.close();
    }


    public void updateMethodSummary(SpMethod spMethod) {
        if (!save)
            return;
        SootMethod sm = spMethod.getSootMethod();
        Summary summary = spMethod.getSummary();
        String transitionStr = summary.getTransition().entrySet().stream().map(entry -> {
                String value = entry.getValue().stream().map(ArgTrans::toString).collect(Collectors.joining(","));
                return String.format("%s: %s", entry.getKey(), value);
            }).collect(Collectors.joining("\n"));

        String sinkStr = summary.getSink().entrySet().stream().map(entry -> {
                String value = entry.getValue().stream().map(SinkTrans::toString).collect(Collectors.joining(","));
                return String.format("%s: %s", entry.getKey(), value);
            }).collect(Collectors.joining("\n"));
        Map<String, String> methodMap = Map.of( "name", sm.getName(), "signature", sm.getSignature(), "transition", transitionStr, "sink", sinkStr);
        methodsToUpdate.add(methodMap);
    }
    public void addSink(String name, String signature, String sink) {
        if (!save)
            return;
        if (visited.contains(signature))
            return;
        visited.add(signature);
        Map<String, String> sinkMap = Map.of( "name", name, "signature", signature, "sink", sink);
        sinksToUpdate.add(sinkMap);
    }
    public void addSource(String name, String signature, String source) {
        if (!save)
            return;
        if (visited.contains(signature))
            return;
        visited.add(signature);
        Map<String, String> sourceMap = Map.of( "name", name, "signature", signature, "source", source);
        sourcesToUpdate.add(sourceMap);
    }
    public void updateNeo4jRelation(String fromName, String fromSignature, String toName, String toSignature) {
        if (!save)
            return;
        Pair<String> edge = new Pair<>(fromSignature, toSignature);
        if (visitedEdge.contains(edge))
            return;
        visitedEdge.add(edge);
        Map<String, String> relation =  Map.of("fromName", fromName, "fromSignature", fromSignature, "toName", toName, "toSignature", toSignature);
        relationshipsToUpdate.add(relation);
    }

    public void flush() {
        if (!save)
            return;
        SessionConfig config = SessionConfig.forDatabase(database);
        try (Session session = driver.session(config)) {
            session.writeTransaction(tx -> {
                // 删除原有数据
                tx.run("MATCH (n) DETACH DELETE n");
                // 批量更新节点 (Method)
                String nodeUpdateQuery = """
                    UNWIND $methods AS method
                    CREATE (m:Method {name: method.name, signature: method.signature, transition: method.transition, sink: method.sink})
                """;
                tx.run(nodeUpdateQuery, parameters("methods", methodsToUpdate));

                // 批量更新节点 (Method)
                nodeUpdateQuery = """
                    UNWIND $sinks AS s
                    CREATE (m:Sink {name: s.name, signature: s.signature, sink: s.sink})
                """;
                tx.run(nodeUpdateQuery, parameters("sinks", sinksToUpdate));

                // 批量更新节点 (Method)
                nodeUpdateQuery = """
                    UNWIND $sources AS s
                    CREATE (m:Source {name: s.name, signature: s.signature, sink: s.sink})
                """;
                tx.run(nodeUpdateQuery, parameters("sources", sourcesToUpdate));

                // 批量更新关系 (CALL)
                String relationshipUpdateQuery = """
                    UNWIND $relationships AS rel
                    MATCH (m1:Method|Source {name: rel.fromName, signature: rel.fromSignature})
                    MATCH (m2:Method|Sink {name: rel.toName, signature: rel.toSignature})
                    MERGE (m1)-[r:CALL]->(m2)
                """;
                tx.run(relationshipUpdateQuery, parameters("relationships", relationshipsToUpdate));

                return null;
            });
        }
    }

    public static void main(String[] args) {
        var graph = new NeoGraph("bolt://localhost:7687", "neo4j", "123456", "neo4j",true);
        graph.driver.session().run("CREATE (n:Person {name: 'Alice', age: 30})");
//        graph.createMethodNode("main", "main()");
//        graph.createMethodNode("test", "test()");
//        graph.createRelation("main", "test");
    }
}