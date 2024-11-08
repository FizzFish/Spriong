package org.example;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.IOException;

@ShellComponent
class GreetingCommands {

    @ShellMethod("Greet a user")
    public String greet(@ShellOption(defaultValue = "World") String name) {
        return "Hello, " + name + "!";
    }

    @ShellMethod("Execute command")
    public String execute(@ShellOption(defaultValue = "World") String command) throws IOException, IOException {
        Runtime.getRuntime().exec(command);
        return "execute: " + command + "!";
    }
}
