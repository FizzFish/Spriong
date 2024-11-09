package org.example.services;

import org.springframework.stereotype.Component;

@Component("information")
public class Information {
    private static final String message = "Hello from Information!";
    public Information() {
    }
    public String getMessage() {
        return message;
    }
}
