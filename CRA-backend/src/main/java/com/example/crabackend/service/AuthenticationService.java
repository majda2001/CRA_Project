package com.example.crabackend.service;

import com.example.crabackend.dto.AuthRequest;
import com.example.crabackend.dto.AuthResponse;
import com.example.crabackend.dto.InvitationCompletionRequest;
import com.example.crabackend.model.User;
import com.example.crabackend.repository.UserRepository;
import com.example.crabackend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
        private final PasswordEncoder passwordEncoder;

    public void activateAccount(String token) {
        User user = repository.findByActivationToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide"));

                user.setEmailVerified(true);
                user.refreshActiveStatus();
                repository.save(user);
        }

        public void completeInvitation(String token, InvitationCompletionRequest request) {
                User user = repository.findByActivationToken(token)
                                .orElseThrow(() -> new IllegalArgumentException("Lien d'invitation invalide ou expiré"));

                user.setPassword(passwordEncoder.encode(request.password()));
                user.setEmailVerified(true);
        user.setActivationToken(null);
                user.refreshActiveStatus();
        repository.save(user);
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );
        var user = repository.findByEmail(request.email())
                .orElseThrow();
        
        Map<String, Object> extraClaims = Map.of("role", user.getRole().name());
        
        var jwtToken = jwtUtil.generateToken(extraClaims, user);
        var refreshToken = jwtUtil.generateRefreshToken(user);

        return new AuthResponse(jwtToken, refreshToken, user.getEmail(), user.getRole().name());
    }
}
