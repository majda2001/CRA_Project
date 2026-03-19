package com.example.crabackend.controller;

import com.example.crabackend.dto.ActivityUpdateRequest;
import com.example.crabackend.dto.CraDto;
import com.example.crabackend.model.User;
import com.example.crabackend.repository.UserRepository;
import com.example.crabackend.service.CraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cra")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COLLABORATEUR')")
public class CraController {

    private final CraService craService;
    private final UserRepository userRepository;

    @GetMapping("/config")
    public ResponseEntity<com.example.crabackend.dto.CraConfigurationDto> getConfiguration() {
        return ResponseEntity.ok(craService.getConfiguration());
    }

    @GetMapping
    public ResponseEntity<CraDto> getCurrentCra(
            Authentication authentication,
            @RequestParam Integer mois,
            @RequestParam Integer annee
    ) {
        User user = getCurrentUser(authentication);
        return ResponseEntity.ok(craService.getOrCreateCra(user.getId(), mois, annee));
    }

    @PutMapping("/{craId}/activities")
    public ResponseEntity<CraDto> updateActivities(
            Authentication authentication,
            @PathVariable Long craId,
            @Valid @RequestBody List<ActivityUpdateRequest> requests
    ) {
        User user = getCurrentUser(authentication);
        return ResponseEntity.ok(craService.updateActivities(user.getId(), craId, requests));
    }

    @PostMapping("/{craId}/submit")
    public ResponseEntity<CraDto> submitCra(
            Authentication authentication,
            @PathVariable Long craId
    ) {
        User user = getCurrentUser(authentication);
        return ResponseEntity.ok(craService.submitCra(user.getId(), craId));
    }

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouve"));
    }
}
