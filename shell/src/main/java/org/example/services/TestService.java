package org.example.services;

import org.springframework.stereotype.Component;

@Component("testService")
public class TestService implements DIYService {
    private final Information information;
    public TestService(Information information) {
        this.information = information;
    }
    public String getMessage() {
        return information.getMessage();
    }
    public void show() {
        System.out.println(getMessage());
    }
}
