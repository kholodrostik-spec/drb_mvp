package com.drb.DrbMVP.controller;

import com.drb.DrbMVP.repository.ApiLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs")
@Tag(name = "API Logs", description = "Manage request history")
public class ApiLogController {

    private final ApiLogRepository apiLogRepository;

    public ApiLogController(ApiLogRepository apiLogRepository) {
        this.apiLogRepository = apiLogRepository;
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete one log by ID")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        apiLogRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Delete all")
    public ResponseEntity<Void> deleteAll() {
        apiLogRepository.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
