package com.example.crabackend.service;

import com.example.crabackend.dto.CollaborateurRequest;
import com.example.crabackend.dto.CollaborateurResponse;
import com.example.crabackend.model.Role;
import com.example.crabackend.model.User;
import com.example.crabackend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollaborateurService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.frontend.activation-url:http://localhost:4200/activate-account}")
    private String activationBaseUrl;

    @Transactional(readOnly = true)
    public List<CollaborateurResponse> getAllCollaborateurs() {
        return repository.findAll().stream()
                .filter(u -> u.getRole() == Role.COLLABORATEUR)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CollaborateurResponse createCollaborateur(CollaborateurRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Cet email est deja utilise par un autre collaborateur.");
        }

        // Generate secure random token for activation
        String token = UUID.randomUUID().toString();
        // Temporary random password (will be overwritten on activation)
        String tempPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(tempPassword)
                .role(Role.COLLABORATEUR)
                .statut(request.statut())
                .contrat(request.contrat())
                .seniorite(request.seniorite())
                .salaire(request.salaire())
                .emailVerified(false) // Email not verified until activation
                .adminAuthorized(true) // Authorized by admin creation
                .active(true) // Account is active but pending activation
                .activationToken(token)
                .build();

        user = repository.save(user);

        // Send activation email
        try {
            String activationLink = activationBaseUrl + "?token=" + token;
            emailService.sendAccountActivationEmail(user.getEmail(), activationLink);
        } catch (Exception e) {
            // Log error but don't fail creation if email service is down (unless critical requirement)
            e.printStackTrace();
        }

        return mapToResponse(user);
    }

    @Transactional
    public CollaborateurResponse updateCollaborateur(Long id, CollaborateurRequest request) {
        User user = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouve"));

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setStatut(request.statut());
        user.setContrat(request.contrat());
        user.setSeniorite(request.seniorite());
        user.setSalaire(request.salaire());

        return mapToResponse(repository.save(user));
    }

    @Transactional
    public void toggleActivation(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouve"));

        boolean willBeActive = !user.isAdminAuthorized();
        user.setAdminAuthorized(willBeActive);

        if (willBeActive) {
            String token = UUID.randomUUID().toString();
            user.setActivationToken(token);
            try {
                String link = activationBaseUrl + "?token=" + token;
                emailService.sendAccountActivationEmail(user.getEmail(), link);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        user.refreshActiveStatus();
        repository.save(user);
    }

    private CollaborateurResponse mapToResponse(User user) {
        return new CollaborateurResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getStatut(),
                user.getContrat(),
                user.getSeniorite(),
                user.getSalaire(),
                user.isEnabled()
        );
    }
}
