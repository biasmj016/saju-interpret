package com.saju.interpret.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/saju")
public class SajuController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "saju-interpret");
    }

    @GetMapping("/{id}")
    public Map<String, Object> getSaju(@PathVariable String id) {
        return Map.of(
            "id", id,
            "message", "Saju interpretation for: " + id,
            "timestamp", System.currentTimeMillis()
        );
    }

    @GetMapping("/interpret/{birthDate}")
    public Map<String, Object> interpret(@PathVariable String birthDate) {
        return Map.of(
            "birthDate", birthDate,
            "interpretation", "Sample interpretation for: " + birthDate,
            "timestamp", System.currentTimeMillis()
        );
    }
}
