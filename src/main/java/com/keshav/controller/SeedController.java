package com.keshav.controller;

import com.keshav.config.DataSeeder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/seed")
public class SeedController {

    private final DataSeeder dataSeeder;

    public SeedController(DataSeeder dataSeeder) {
        this.dataSeeder = dataSeeder;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> seedDatabase() {
        Map<String, Object> result = dataSeeder.seedAll();
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> seedDatabaseGet() {
        Map<String, Object> result = dataSeeder.seedAll();
        return ResponseEntity.ok(result);
    }
}
