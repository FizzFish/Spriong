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
        Map<String, String> methodMap = Map.of( "name", sm.getName(), "signature", sm.getSignature(), "transition", transitionStr, "sink", sinkStr,"color", "blue");
        methodsToUpdate.add(methodMap);
    }
    public void addSink(String name, String signature, String sink) {
        if (!save)
            return;
        if (visited.contains(signature))
            return;
        visited.add(signature);
        Map<String, String> methodMap = Map.of( "name", name, "signature", signature, "transition", "", "sink", sink, "color", "red");
        methodsToUpdate.add(methodMap);
    }
    public void updateNeo4jRelation(SootMethod caller, SootMethod callee) {
        if (!save)
            return;
        String fromName = caller.getName();
        String fromSignature = caller.getSignature();
        String toName = callee.getName();
        String toSignature = callee.getSignature();
        internalEdgeUpdate(fromName, fromSignature, toName, toSignature);
    }
    public void internalEdgeUpdate(String fromName, String fromSignature, String toName, String toSignature) {
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

                // 批量更新关系 (CALL)
                String relationshipUpdateQuery = """
                    UNWIND $relationships AS rel
                    MATCH (m1:Method {name: rel.fromName, signature: rel.fromSignature})
                    MATCH (m2:Method {name: rel.toName, signature: rel.toSignature})
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