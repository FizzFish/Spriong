package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.standard.ShellComponent;

@SpringBootApplication(scanBasePackages = "org.example")
public class Application {
    private final ServiceWrapper wrapper;
    public Application(ServiceWrapper wrapper) {
        this.wrapper = wrapper;
    }
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

