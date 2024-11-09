package org.example;

import org.example.services.DIYService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("serviceWrapper")
public class ServiceWrapper {
    private static class ServiceHolder {
        String name;
        DIYService service;
    }
    public ServiceWrapper(List<DIYService> services) {
        System.out.println("ServiceWrapper init");
        services.forEach(DIYService::show);
    }
}
