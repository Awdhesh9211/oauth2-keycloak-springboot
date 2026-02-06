package com.learn.resourceserver;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataController {

    @GetMapping("/data")
    public String getData() {
        return "Hello From Resource Server";
    }
}

