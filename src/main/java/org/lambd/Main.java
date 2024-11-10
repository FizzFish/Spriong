package org.lambd;

import org.yaml.snakeyaml.Yaml;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.util.Chain;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        SootWorld sootWorld = SootWorld.v();
        String configFile = args[0];
        System.out.println("parse config: " + configFile);
        Config config = loadConfig(configFile);
        sootWorld.setConfig(config);
        sootWorld.initSootEnv();

        sootWorld.analyze();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("Execution time: " + duration + " milliseconds");
        sootWorld.statistics();
    }
    public static Config loadConfig(String filePath) {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: " + filePath);
            }
            return yaml.loadAs(inputStream, Config.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}






