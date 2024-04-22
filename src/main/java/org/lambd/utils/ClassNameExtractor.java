package org.lambd.utils;

import com.google.common.collect.Lists;
import soot.*;
import soot.options.Options;
import soot.util.Chain;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class ClassNameExtractor {
    private static final String JAR = ".jar";

    private static final String CLASS = ".class";

    private static final String JAVA = ".java";
    /**
     * Extracts names of all classes in given path.
     */
    public static List<String> extract(String path) {
        return path.endsWith(JAR) ? extractJar(path) : extractDir(path);
    }

    private static List<String> extractJar(String jarPath) {
        File file = new File(jarPath);
        try (JarFile jar = new JarFile(file)) {
            System.out.printf("Scanning %s ... ", file.getAbsolutePath());
            List<String> classNames = jar.stream()
                    .filter(e -> !e.getName().startsWith("META-INF"))
                    .filter(e -> e.getName().endsWith(CLASS))
                    .map(e -> {
                        String name = e.getName();
                        return name.replaceAll("/", ".")
                                .substring(0, name.length() - CLASS.length());
                    })
                    .toList();
            System.out.printf("%d classes%n", classNames.size());
            return classNames;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read jar file: " +
                    file.getAbsolutePath(), e);
        }
    }

    private static List<String> extractDir(String dirPath) {
        Path dir = Path.of(dirPath);
        if (!dir.toFile().isDirectory()) {
            throw new RuntimeException(dir + " is not a directory");
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            System.out.printf("Scanning %s ... ", dir.toAbsolutePath());
            List<String> classNames = new ArrayList<>();
            paths.map(dir::relativize).forEach(path -> {
                String fileName = path.getFileName().toString();
                int suffix;
                if (fileName.endsWith(CLASS)) {
                    suffix = CLASS.length();
                } else if (fileName.endsWith(JAVA)) {
                    suffix = JAVA.length();
                } else {
                    return;
                }
                String name = path.toString();
                String className = name.substring(0, name.length() - suffix);
                classNames.add(className);
            });
            System.out.printf("%d classes%n", classNames.size());
            return classNames;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read directory: " + dirPath, e);
        }
    }

}
