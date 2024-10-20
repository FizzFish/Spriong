package org.lambd.transition;

import org.lambd.SpMethod;
import org.neo4j.driver.*;
import soot.SootMethod;

import java.util.Set;
import java.util.stream.Collectors;

import static org.neo4j.driver.Values.parameters;

public class NeoGraph implements AutoCloseable {
    private final Driver driver;
    private Session session;

    public NeoGraph(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        session = driver.session();
    }

    @Override
    public void close() throws RuntimeException {
        driver.close();
    }


    public void updateMethodSummary(SpMethod spMethod) {
//        SootMethod sm = spMethod.getSootMethod();
//        Summary summary = spMethod.getSummary();
//        String transitionStr = summary.getTRansition().entrySet().stream().map(entry -> {
//                String value = entry.getValue().stream().map(ArgTrans::toString).collect(Collectors.joining(","));
//                return String.format("%s: %s", entry.getKey(), value);
//            }).collect(Collectors.joining("\n"));
//
//        String sinkStr = summary.getSink().entrySet().stream().map(entry -> {
//                String value = entry.getValue().stream().map(SinkTrans::toString).collect(Collectors.joining(","));
//                return String.format("%s: %s", entry.getKey(), value);
//            }).collect(Collectors.joining("\n"));
//        this.session.run("MATCH (m:Method {signature: $signature}) SET m.transition = $transition, m.sink = $sink",
//                parameters("signature", sm.getSignature(), "transition", transitionStr, "sink", sinkStr));
    }
    public void createRelationWithMethods(SootMethod caller, SootMethod callee) {
//        String fromName = caller.getName();
//        String fromSignature = caller.getSignature();
//        String toName = callee.getName();
//        String toSignature = callee.getSignature();
//        session.run(
//                "MERGE (m1:Method {name: $fromName, signature: $fromSignature}) " +
//                        "MERGE (m2:Method {name: $toName, signature: $toSignature}) " +
//                        "MERGE (m1)-[r:CALL]->(m2)",
//                parameters("fromName", fromName, "fromSignature", fromSignature, "toName", toName, "toSignature", toSignature)
//        );
    }

    public void clear() {
//        session.run("MATCH (n) DETACH DELETE n");
    }

    public static void main(String[] args) {
        var graph = new NeoGraph("bolt://localhost:7687", "neo4j", "123456");
//        graph.createMethodNode("main", "main()");
//        graph.createMethodNode("test", "test()");
//        graph.createRelation("main", "test");
    }
}