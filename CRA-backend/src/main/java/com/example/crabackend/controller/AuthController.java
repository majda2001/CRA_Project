package com.example.crabackend.controller;

import com.example.crabackend.dto.AuthRequest;
import com.example.crabackend.dto.AuthResponse;
import com.example.crabackend.dto.InvitationCompletionRequest;
import com.example.crabackend.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService service;

    @GetMapping("/activate")
    public ResponseEntity<?> activateAccount(@RequestParam String token) {
        service.activateAccount(token);
        return ResponseEntity.ok(Map.of("message", "Compte activé avec succès. Vous pouvez maintenant vous connecter."));
    }

    @PostMapping("/invitation/complete")
    public ResponseEntity<?> completeInvitation(
            @RequestParam String token,
            @Valid @RequestBody InvitationCompletionRequest request
    ) {
        service.completeInvitation(token, request);
        return ResponseEntity.ok(Map.of("message", "Invitation validée. Vous pouvez maintenant vous connecter."));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }
}
