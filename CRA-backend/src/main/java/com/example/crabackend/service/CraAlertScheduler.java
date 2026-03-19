package com.example.crabackend.service;

import com.example.crabackend.config.AdminProperties;
import com.example.crabackend.model.Cra;
import com.example.crabackend.model.CraStatus;
import com.example.crabackend.model.Role;
import com.example.crabackend.model.User;
import com.example.crabackend.repository.CraRepository;
import com.example.crabackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CraAlertScheduler {

    private final UserRepository userRepository;
    private final CraRepository craRepository;
    private final EmailService emailService;
    private final AdminProperties adminProperties;

    @Value("${app.timezone:Europe/Paris}")
    private String appTimezone;

    @Scheduled(cron = "0 0 10 28 * ?", zone = "${app.timezone:Europe/Paris}")
    public void verificationSoumissionCra() {
        LocalDate today = LocalDate.now(ZoneId.of(appTimezone));
        List<String> missing = verificationSoumissionCra(today.getMonthValue(), today.getYear());
        log.info("Fin du job CRA: {} collaborateur(s) non soumis", missing.size());
    }

    public List<String> verificationSoumissionCra(Integer mois, Integer annee) {
        List<String> notSubmittedCollaborateurs = new ArrayList<>();

        log.info("Lancement du job de verification des CRA pour {}/{}", mois, annee);

        List<User> collaborateurs = userRepository.findAllByRole(Role.COLLABORATEUR);
        for (User collab : collaborateurs) {
            Optional<Cra> craOpt = craRepository.findByCollaborateurAndMoisAndAnnee(collab, mois, annee);
            boolean submitted = craOpt.isPresent()
                    && (craOpt.get().getStatus() == CraStatus.SUBMITTED || craOpt.get().getStatus() == CraStatus.APPROVED);

            if (!submitted) {
                String fullName = (safe(collab.getFirstName()) + " " + safe(collab.getLastName())).trim();
                notSubmittedCollaborateurs.add(fullName + " <" + collab.getEmail() + ">");
            }
        }

        if (!notSubmittedCollaborateurs.isEmpty() && adminProperties.getEmail() != null && !adminProperties.getEmail().isBlank()) {
            String subject = "CRA non soumis - " + String.format("%02d/%d", mois, annee);
            String body = "Collaborateurs sans CRA soumis:\n\n" +
                    notSubmittedCollaborateurs.stream().collect(Collectors.joining("\n"));

            try {
                emailService.sendSimpleEmail(adminProperties.getEmail(), subject, body);
            } catch (Exception e) {
                log.warn("Impossible d'envoyer l'email d'alerte CRA", e);
            }
        }

        log.info("Job CRA termine: {} collaborateur(s) non soumis", notSubmittedCollaborateurs.size());
        return notSubmittedCollaborateurs;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
