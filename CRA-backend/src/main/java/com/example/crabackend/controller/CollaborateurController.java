package com.example.crabackend.controller;

import com.example.crabackend.dto.CollaborateurRequest;
import com.example.crabackend.dto.CollaborateurResponse;
import com.example.crabackend.service.CollaborateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/collaborateurs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CollaborateurController {

    private final CollaborateurService service;

    @GetMapping
    public ResponseEntity<List<CollaborateurResponse>> getAll() {
        return ResponseEntity.ok(service.getAllCollaborateurs());
    }

    @PostMapping
    public ResponseEntity<CollaborateurResponse> create(@Valid @RequestBody CollaborateurRequest request) {
        return ResponseEntity.ok(service.createCollaborateur(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CollaborateurResponse> update(@PathVariable Long id, @Valid @RequestBody CollaborateurRequest request) {
        return ResponseEntity.ok(service.updateCollaborateur(id, request));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> toggleActivation(@PathVariable Long id) {
        service.toggleActivation(id);
        return ResponseEntity.ok().build();
    }
}
