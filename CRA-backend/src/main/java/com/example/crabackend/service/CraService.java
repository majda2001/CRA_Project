package com.example.crabackend.service;

import com.example.crabackend.dto.ActivityDto;
import com.example.crabackend.dto.ActivityUpdateRequest;
import com.example.crabackend.dto.CraDto;
import com.example.crabackend.model.Activity;
import com.example.crabackend.model.ActivityType;
import com.example.crabackend.model.Assignment;
import com.example.crabackend.model.Cra;
import com.example.crabackend.model.CraStatus;
import com.example.crabackend.model.Mission;
import com.example.crabackend.model.User;
import com.example.crabackend.repository.AssignmentRepository;
import com.example.crabackend.repository.CraRepository;
import com.example.crabackend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CraService {

    private static final ZoneId PARIS_ZONE = ZoneId.of("Europe/Paris");

    @Value("${cra.submission.window.start-day:22}")
    private int windowStartDay;

    @Value("${cra.submission.window.end-day:28}")
    private int windowEndDay;

    @jakarta.annotation.PostConstruct
    public void init() {
        System.out.println(">>> DEBUG: CRA Window Configured: Start=" + windowStartDay + ", End=" + windowEndDay);
    }

    public com.example.crabackend.dto.CraConfigurationDto getConfiguration() {
        return new com.example.crabackend.dto.CraConfigurationDto(windowStartDay, windowEndDay);
    }

    private final CraRepository craRepository;
    private final UserRepository userRepository;
    private final AssignmentRepository assignmentRepository;

    @Transactional
    public CraDto getOrCreateCra(Long userId, Integer mois, Integer annee) {
        User user = getCollaborateur(userId);
        Cra cra = craRepository.findByCollaborateurAndMoisAndAnnee(user, mois, annee)
                .orElseGet(() -> craRepository.save(Cra.builder()
                        .collaborateur(user)
                        .mois(mois)
                        .annee(annee)
                        .status(CraStatus.DRAFT)
                        .activities(new ArrayList<>())
                        .build()));
        return toDto(cra);
    }

    @Transactional
    public CraDto updateActivities(Long userId, Long craId, List<ActivityUpdateRequest> requests) {
        Cra cra = getCraOwnedBy(craId, userId);
        if (cra.getStatus() == CraStatus.APPROVED) {
            throw new IllegalStateException("CRA non modifiable dans son etat actuel");
        }

        // CRA is editable ONLY during the declaration window (DYNAMIC), UNLESS it is INVALIDATED.
        int currentDay = LocalDate.now(PARIS_ZONE).getDayOfMonth();
        boolean inWindow = currentDay >= windowStartDay && currentDay <= windowEndDay;
        boolean isInvalidated = cra.getStatus() == CraStatus.INVALIDATED;
        
        if (!inWindow && !isInvalidated) {
             throw new IllegalStateException("La saisie et la modification du CRA ne sont autorisees qu'entre le " + windowStartDay + " et le " + windowEndDay + " du mois, sauf en cas d'invalidation.");
        }

        Map<LocalDate, Activity> activitiesByDate = cra.getActivities().stream()
                .collect(Collectors.toMap(Activity::getDate, a -> a, (a, b) -> a, LinkedHashMap::new));

        for (ActivityUpdateRequest request : requests) {
            LocalDate targetDate = request.date();
            ensureDateInCraMonth(cra, targetDate);

            // Weekends (Saturday=6, Sunday=7) are strictly ignored.
            if (targetDate.getDayOfWeek().getValue() >= 6) {
                continue;
            }

            ActivityNormalized normalized = normalizeActivity(userId, request);

            Activity activity = Activity.builder()
                    .date(targetDate)
                    .duree(request.duree())
                    .type(normalized.type())
                    .mission(normalized.mission())
                    .cra(cra)
                    .build();
            activitiesByDate.put(targetDate, activity);
        }

        cra.getActivities().clear();
        cra.getActivities().addAll(activitiesByDate.values());
        cra.setStatus(CraStatus.DRAFT);
        cra.setDateSoumission(null);
        cra.setDateValidation(null);

        return toDto(craRepository.save(cra));
    }

    @Transactional
    public CraDto submitCra(Long userId, Long craId) {
        Cra cra = getCraOwnedBy(craId, userId);
        
        // Window check with exception for INVALIDATED status
        int currentDay = LocalDate.now(PARIS_ZONE).getDayOfMonth();
        boolean inWindow = currentDay >= windowStartDay && currentDay <= windowEndDay;
        boolean isInvalidated = cra.getStatus() == CraStatus.INVALIDATED;

        if (!inWindow && !isInvalidated) {
            throw new IllegalStateException("La soumission du CRA est autorisee uniquement entre le " + windowStartDay + " et le " + windowEndDay + " du mois, sauf en cas d'invalidation.");
        }

        if (cra.getStatus() == CraStatus.APPROVED) {
            throw new IllegalStateException("CRA deja approuve");
        }
        // No checks on DRAFT status here - submission is the ONLY action allowed in the window (or for correction).

        if (cra.getActivities() == null || cra.getActivities().isEmpty()) {
            throw new IllegalArgumentException("Le CRA doit contenir au moins une activite");
        }
        
        // Clear rejection motive on new submission to behave like a fresh submission
        // (unless we want to keep history, but for logic purposes, index is reset)
        // Actually, we should probably keep it until approved? No, if we submit, it's pending again.
        // We clear it so "isCorrection" becomes false for subsequent edits if user tries to hack.
        // But importantly, once submitted, it is locked anyway by status != DRAFT/REJECTED.
        // Let's clear it to be clean.
        // cra.setMotifRejet(null); // Optional: decide if we want to clear it. Let's NOT clear it yet, 
        // useful for the admin to see "Previous rejection reason". 
        // But if I don't clear it, the user can un-submit (if allowed) or edit if we allow editing submitted... 
        // We don't allow editing submitted.
        // So keeping it is fine.
        
        cra.setStatus(CraStatus.SUBMITTED);
        cra.setDateSoumission(LocalDate.now(PARIS_ZONE));
        return toDto(craRepository.save(cra));
    }

    @Transactional(readOnly = true)
    public List<CraDto> getAllCras(Integer mois, Integer annee, CraStatus status) {
        return craRepository.findAll().stream()
                .filter(cra -> mois == null || mois.equals(cra.getMois()))
                .filter(cra -> annee == null || annee.equals(cra.getAnnee()))
                .filter(cra -> status == null || status == cra.getStatus())
                .sorted(Comparator.comparing(Cra::getAnnee)
                        .thenComparing(Cra::getMois)
                        .reversed())
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CraDto validerCra(Long id) {
        Cra cra = craRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CRA non trouve"));
        cra.setStatus(CraStatus.APPROVED);
        cra.setDateValidation(LocalDate.now(PARIS_ZONE));
        return toDto(craRepository.save(cra));
    }

    @Transactional
    public CraDto rejeterCra(Long id, String motif) {
        Cra cra = craRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CRA non trouve"));
        
        if (motif == null || motif.trim().isEmpty()) {
            throw new IllegalArgumentException("Le motif est obligatoire pour le rejet d'un CRA.");
        }
        
        cra.setStatus(CraStatus.REJECTED);
        cra.setDateValidation(LocalDate.now(PARIS_ZONE));
        cra.setMotifRejet(motif.trim()); // Set the provided motif
        return toDto(craRepository.save(cra));
    }

    @Transactional
    public CraDto invaliderCra(Long id, String motif) {
        Cra cra = craRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CRA non trouve"));
        
        if (motif == null || motif.trim().isEmpty()) {
            throw new IllegalArgumentException("Le motif est obligatoire pour l'invalidation d'un CRA.");
        }
        
        cra.setStatus(CraStatus.INVALIDATED);
        cra.setDateValidation(LocalDate.now(PARIS_ZONE));
        cra.setMotifRejet(motif.trim()); // Set the provided motif
        return toDto(craRepository.save(cra));
    }



    private void ensureDateInCraMonth(Cra cra, LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("La date d'activite est obligatoire");
        }
        YearMonth craMonth = YearMonth.of(cra.getAnnee(), cra.getMois());
        if (!YearMonth.from(date).equals(craMonth)) {
            throw new IllegalArgumentException("La date d'activite doit appartenir au mois du CRA");
        }
    }

    private ActivityNormalized normalizeActivity(Long userId, ActivityUpdateRequest request) {
        ActivityType type = request.type();
        Mission mission = null;

        if (type == ActivityType.CONGE || type == ActivityType.RTT || type == ActivityType.MALADIE) {
            return new ActivityNormalized(type, null);
        }

        // Collaborator cannot manually pick a mission/intercontract.
        // Any non-absence input is normalized from admin assignments for the day.
        {
            List<Assignment> assignments = assignmentRepository.findActiveAssignmentsForDate(userId, request.date());
            if (assignments.isEmpty()) {
                type = ActivityType.INTERCONTRACT;
            } else {
                assignments.sort((a, b) -> {
                    LocalDate aStart = a.getDateDebut();
                    LocalDate bStart = b.getDateDebut();
                    if (aStart == null && bStart == null) {
                        return 0;
                    }
                    if (aStart == null) {
                        return 1;
                    }
                    if (bStart == null) {
                        return -1;
                    }
                    return bStart.compareTo(aStart);
                });
                mission = assignments.get(0).getMission();
                type = ActivityType.MISSION;
            }
        }

        return new ActivityNormalized(type, mission);
    }

    private record ActivityNormalized(ActivityType type, Mission mission) {}

    @Transactional(readOnly = true)
    public String exportCrasToCsv(Integer mois, Integer annee) {
        List<CraDto> rows = getAllCras(mois, annee, null);
        StringBuilder sb = new StringBuilder();
        sb.append("id;collaborateur;email;mois;annee;status;dateSoumission;dateValidation;motifRejet\n");

        for (CraDto cra : rows) {
            sb.append(cra.id()).append(';')
                    .append(csv(cra.collaborateurName())).append(';')
                    .append(csv(cra.collaborateurEmail())).append(';')
                    .append(cra.mois()).append(';')
                    .append(cra.annee()).append(';')
                    .append(cra.status()).append(';')
                    .append(cra.dateSoumission() != null ? cra.dateSoumission() : "").append(';')
                    .append(cra.dateValidation() != null ? cra.dateValidation() : "").append(';')
                    .append(csv(cra.motifRejet()))
                    .append('\n');
        }

        return sb.toString();
    }

    private User getCollaborateur(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouve"));
    }

    private Cra getCraOwnedBy(Long craId, Long userId) {
        Cra cra = craRepository.findById(craId)
                .orElseThrow(() -> new EntityNotFoundException("CRA non trouve"));
        if (!cra.getCollaborateur().getId().equals(userId)) {
            throw new IllegalArgumentException("Ce CRA ne vous appartient pas");
        }
        return cra;
    }

    private CraDto toDto(Cra cra) {
        List<ActivityDto> activities = cra.getActivities().stream()
                .sorted(Comparator.comparing(Activity::getDate))
                .map(activity -> new ActivityDto(
                        activity.getId(),
                        activity.getDate(),
                        activity.getDuree(),
                        activity.getType(),
                        activity.getMission() != null ? activity.getMission().getId() : null,
                        activity.getMission() != null ? activity.getMission().getTitre() : null
                ))
                .collect(Collectors.toList());

        return new CraDto(
                cra.getId(),
                cra.getCollaborateur().getId(),
                fullName(cra.getCollaborateur()),
                cra.getCollaborateur().getEmail(),
                cra.getMois(),
                cra.getAnnee(),
                cra.getStatus(),
                cra.getDateSoumission(),
                cra.getDateValidation(),
                cra.getMotifRejet(),
                activities
        );
    }

    private String fullName(User user) {
        String first = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String last = user.getLastName() != null ? user.getLastName().trim() : "";
        return (first + " " + last).trim();
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replace("\"", "\"\"");
        boolean needsQuotes = normalized.contains(";") || normalized.contains("\n") || normalized.contains("\r");
        return needsQuotes ? '"' + normalized + '"' : normalized;
    }
}
