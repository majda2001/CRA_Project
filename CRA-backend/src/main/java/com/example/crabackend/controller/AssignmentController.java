package com.example.crabackend.controller;

import com.example.crabackend.dto.AssignmentRequest;
import com.example.crabackend.dto.AssignmentResponse;
import com.example.crabackend.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/assignments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AssignmentController {

    private final AssignmentService service;

    @GetMapping
    public ResponseEntity<List<AssignmentResponse>> getAllAssignments() {
        return ResponseEntity.ok(service.getAllAssignments());
    }

    @PostMapping
    public ResponseEntity<AssignmentResponse> createAssignment(@Valid @RequestBody AssignmentRequest request) {
        return ResponseEntity.ok(service.createAssignment(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssignmentResponse> updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentRequest request) {
        return ResponseEntity.ok(service.updateAssignment(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        service.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/collaborateur/{userId}")
    public ResponseEntity<List<AssignmentResponse>> getByCollaborateur(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getAssignmentsByCollaborateur(userId));
    }
}
