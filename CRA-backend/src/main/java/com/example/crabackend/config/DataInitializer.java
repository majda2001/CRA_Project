package com.example.crabackend.config;

import com.example.crabackend.model.*;
import com.example.crabackend.repository.*;
import com.example.crabackend.service.EmailService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.UUID;


@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final MissionRepository missionRepository;
    private final AssignmentRepository assignmentRepository;
    private final CraRepository craRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;
    private final EmailService emailService;

    @Value("${app.timezone:Europe/Paris}")
    private String appTimezone;

    @Value("${app.demo.enabled:true}")
    private boolean demoEnabled;

    @Value("${app.demo.default-password:Demo123!}")
    private String demoPassword;

    @Value("${app.demo.admin-email:demo.admin@cbx.local}")
    private String demoAdminEmail;

    @Value("${app.demo.admin-password:DemoAdmin123!}")
    private String demoAdminPassword;

    @Value("${app.demo.send-invitation-emails:true}")
    private boolean demoSendInvitationEmails;

    @Value("${app.frontend.activation-url:http://localhost:4200/activate-account}")
    private String activationBaseUrl;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @PostConstruct
    public void init() {
        initAdmin();
        initDemoData();
    }

    private void initAdmin() {
        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }

        String adminEmail = adminProperties.getEmail();
        String adminPassword = adminProperties.getPassword();

        if ((adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) && demoEnabled) {
            adminEmail = demoAdminEmail;
            adminPassword = demoAdminPassword;
        }

        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            return;
        }

        User admin = User.builder()
                .firstName("Admin")
                .lastName("CBX")
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .emailVerified(true)
                .adminAuthorized(true)
                .active(true)
                .build();

        userRepository.save(admin);
    }

    private void initDemoData() {
        if (!demoEnabled) {
            return;
        }

        LocalDate now = LocalDate.now(ZoneId.of(appTimezone));
        int mois = now.getMonthValue();
        int annee = now.getYear();

        User majda = ensureCollaborateur(
                "majda.ablaoui@enim.ac.ma",
                "Majda",
                "Ablaoui",
                "MISSION",
                "CDI",
            "CONFIRME",
            BigDecimal.valueOf(4500)
        );
        User yassine = ensureCollaborateur(
                "yassine.benali@cbx-demo.local",
                "Yassine",
                "Benali",
                "MISSION",
                "PORTAGE",
            "EXPERT",
            BigDecimal.valueOf(6800)
        );
    
        User mehdi = ensureCollaborateur(
                "mehdi.karim@cbx-demo.local",
                "Mehdi",
                "Karim",
                "MISSION",
                "FREELANCE",
            "CONFIRME",
            BigDecimal.valueOf(6200)
        );

        sendDemoInvitationIfEnabled(majda);
        sendDemoInvitationIfEnabled(yassine);
        sendDemoInvitationIfEnabled(mehdi);

        Mission billing = ensureMission("Migration SAP S4", "Migration et fiabilisation des interfaces facturation", now.minusMonths(3), now.plusMonths(4), "Projet client", 650d);
        Mission banking = ensureMission("Portail Banque B2B", "Refonte portail corporate", now.minusMonths(2), now.plusMonths(6), "Transformation digitale", 700d);
        Mission data = ensureMission("Data Quality Program", "Qualite et gouvernance de donnees", now.minusMonths(1), now.plusMonths(2), "Conformite", 620d);

        ensureAssignment(majda, billing);
        ensureAssignment(yassine, banking);
        ensureAssignment(mehdi, data);

          }

    private User ensureCollaborateur(
            String email,
            String firstName,
            String lastName,
            String statut,
            String contrat,
            String seniorite,
            BigDecimal salaire
    ) {
        User existing = userRepository.findByEmail(email).orElse(null);

        if (existing == null) {
            User created = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(demoPassword))
                    .firstName(firstName)
                    .lastName(lastName)
                    .role(Role.COLLABORATEUR)
                    .statut(statut)
                    .contrat(contrat)
                    .seniorite(seniorite)
                    .salaire(salaire)
                    .emailVerified(true)
                    .adminAuthorized(true)
                    .active(true)
                    .activationToken(UUID.randomUUID().toString())
                    .build();
            created.refreshActiveStatus();
                    return userRepository.save(created);
        }

        existing.setFirstName(firstName);
        existing.setLastName(lastName);
        existing.setRole(Role.COLLABORATEUR);
        existing.setStatut(statut);
        existing.setContrat(contrat);
        existing.setSeniorite(seniorite);
        existing.setSalaire(salaire);
        existing.setEmailVerified(true);
        existing.setAdminAuthorized(true);
        if (existing.getPassword() == null || existing.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(demoPassword));
        }
        if (existing.getActivationToken() == null || existing.getActivationToken().isBlank()) {
            existing.setActivationToken(UUID.randomUUID().toString());
        }
        existing.refreshActiveStatus();
        return userRepository.save(existing);
    }

    private void sendDemoInvitationIfEnabled(User user) {
        if (!demoSendInvitationEmails) {
            return;
        }

        if (user.getActivationToken() == null || user.getActivationToken().isBlank()) {
            return;
        }

        if (smtpUsername == null || smtpUsername.isBlank()) {
            log.warn("Email demo non envoye pour {}: SMTP_USERNAME non configure.", user.getEmail());
            return;
        }

        try {
            String link = activationBaseUrl + "?token=" + user.getActivationToken();
            emailService.sendAccountActivationEmail(user.getEmail(), link);
            log.info("Invitation demo envoyee a {}", user.getEmail());
        } catch (Exception ex) {
            log.error("Echec envoi invitation demo a {}: {}", user.getEmail(), ex.getMessage());
        }
    }

    private Mission ensureMission(String title, String description, LocalDate start, LocalDate end, String motif, Double tjm) {
        return missionRepository.findAll().stream()
                .filter(m -> title.equalsIgnoreCase(m.getTitre()))
                .findFirst()
                .orElseGet(() -> missionRepository.save(Mission.builder()
                        .titre(title)
                        .description(description)
                        .dateDebut(start)
                        .dateFin(end)
                        .motif(motif)
                        .tjm(tjm)
                        .build()));
    }

    private void ensureAssignment(User collaborateur, Mission mission) {
        LocalDate start = mission.getDateDebut();
        LocalDate end = mission.getDateFin();

        boolean exists = assignmentRepository.findByCollaborateur(collaborateur).stream()
                .anyMatch(a -> a.getMission() != null
                        && mission.getId().equals(a.getMission().getId())
                        && start.equals(a.getDateDebut())
                        && ((a.getDateFin() == null && end == null) || (a.getDateFin() != null && a.getDateFin().equals(end))));
        if (exists) {
            return;
        }

        assignmentRepository.save(Assignment.builder()
                .collaborateur(collaborateur)
                .mission(mission)
                .dateDebut(start)
                .dateFin(end)
                .build());
    }

    private void ensureCra(User collaborateur, int mois, int annee, CraStatus status, String motifRejet, Mission primaryMission) {
        if (craRepository.findByCollaborateurAndMoisAndAnnee(collaborateur, mois, annee).isPresent()) {
            return;
        }

        Cra cra = Cra.builder()
                .collaborateur(collaborateur)
                .mois(mois)
                .annee(annee)
                .status(status)
                .motifRejet(motifRejet)
                .activities(new ArrayList<>())
                .build();

        LocalDate first = LocalDate.of(annee, mois, 1);
        

        if (status == CraStatus.SUBMITTED || status == CraStatus.APPROVED || status == CraStatus.INVALIDATED) {
            cra.setDateSoumission(LocalDate.of(annee, mois, Math.min(24, first.lengthOfMonth())));
        }
        if (status == CraStatus.APPROVED || status == CraStatus.INVALIDATED) {
            cra.setDateValidation(LocalDate.of(annee, mois, Math.min(26, first.lengthOfMonth())));
        }

        craRepository.save(cra);
    }

    private void addActivity(Cra cra, LocalDate date, ActivityType type, Mission mission, Double duree) {
        Activity activity = Activity.builder()
                .cra(cra)
                .date(date)
                .type(type)
                .mission(mission)
                .duree(duree)
                .build();
        cra.getActivities().add(activity);
    }

}
