package org.lambd.utils;

import com.google.common.collect.Lists;
import soot.*;
import soot.options.Options;
import soot.util.Chain;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
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
    public static String extractBootInfClasses(String jarPath) throws IOException {
        String jar = jarPath.substring(0, jarPath.lastIndexOf("!"));
        String packageName = jarPath.substring(jarPath.lastIndexOf("!") + 1);
        JarFile jarFile = new JarFile(jar);
        String pathString = "src/main/resources/app-classes";
        Path targetDir = Paths.get(pathString);
        // 如果目录已存在，先删除
        if (Files.exists(targetDir)) {
            Files.walk(targetDir)
                    .sorted((path1, path2) -> path2.compareTo(path1))  // 先删除子文件或子目录
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            System.err.println("Failed to delete " + path + ": " + e.getMessage());
                        }
                    });
        }
        // 创建目录
        Files.createDirectories(targetDir);

        if (!packageName.endsWith("classes")) {
            targetDir = Paths.get(pathString, packageName);
            Files.createDirectories(targetDir);
        }


        // 解压 BOOT-INF/classes 下的所有文件
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().startsWith(packageName) && !entry.isDirectory()) {
                File classFile = new File(targetDir.toFile(), entry.getName().substring(packageName.length()));
                classFile.getParentFile().mkdirs();
                try (InputStream input = jarFile.getInputStream(entry);
                     FileOutputStream output = new FileOutputStream(classFile)) {
                    input.transferTo(output);
                }
            }
        }
        jarFile.close();

        return pathString;
    }

    public static void processJarFile(String jarFilePath, String outputPath, String prefix) {
        File jarFile = new File(jarFilePath);
        File outputDir = new File(outputPath);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith("BOOT-INF/classes/"))
                    name = name.substring("BOOT-INF/classes/".length());

                if (name.endsWith(".class") && name.startsWith(prefix)) {
                    copyTo(jar.getInputStream(entry), name, outputDir);
                } else if (name.endsWith(".jar")) {
                    try (InputStream is = jar.getInputStream(entry)) {
                        processNestedJar(is, outputDir, prefix);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static void processNestedJar(InputStream is, File outputDir, String prefix) throws IOException {
        try (JarInputStream jis = new JarInputStream(is)) {
            JarEntry embeddedEntry;
            while ((embeddedEntry = jis.getNextJarEntry()) != null) {
                String name = embeddedEntry.getName();

                if (name.endsWith(".class") && name.startsWith(prefix)) {
                    copyTo(jis, name, outputDir);
                }
            }
        }
    }

    private static void copyTo(InputStream is, String outputPath, File outputDir) throws IOException {
        File outputFile = new File(outputDir, outputPath);
        // 确保父目录存在
        File parentDir = outputFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        FileOutputStream fos = new FileOutputStream(outputFile);
        is.transferTo(fos);
    }
}
