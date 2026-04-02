package com.judy.secureprojecthub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Hello World", description = "Basic API endpoints for testing and health checks")
public class HelloWorldController {

    @GetMapping("/hello")
    @Operation(summary = "Hello World Endpoint", description = "Returns a simple hello world message")
    public String hello() {
        return "Hello, World!";
    }

    @GetMapping("/status")
    @Operation(summary = "Application Status", description = "Returns the current status of the Secure Project Hub application")
    public String status() {
        return "Secure Project Hub is running!";
    }
}
