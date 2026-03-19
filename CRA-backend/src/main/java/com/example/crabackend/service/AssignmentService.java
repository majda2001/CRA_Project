package com.example.crabackend.service;

import com.example.crabackend.dto.AssignmentRequest;
import com.example.crabackend.dto.AssignmentResponse;
import com.example.crabackend.model.Assignment;
import com.example.crabackend.model.Mission;
import com.example.crabackend.model.User;
import com.example.crabackend.repository.AssignmentRepository;
import com.example.crabackend.repository.MissionRepository;
import com.example.crabackend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final MissionRepository missionRepository;

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAllAssignments() {
        return assignmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AssignmentResponse createAssignment(AssignmentRequest request) {
        User collaborateur = userRepository.findById(request.collaborateurId())
                .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouve"));

        Mission mission = missionRepository.findById(request.missionId())
                .orElseThrow(() -> new EntityNotFoundException("Mission non trouvee"));

        // Force mission dates
        LocalDate start = mission.getDateDebut();
        LocalDate end = mission.getDateFin();

        validatePeriod(start, end);

        Assignment assignment = Assignment.builder()
                .collaborateur(collaborateur)
                .mission(mission)
                .dateDebut(start)
                .dateFin(end)
                .build();

        return mapToResponse(assignmentRepository.save(assignment));
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsByCollaborateur(Long userId) {
        User collab = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouve"));
        return assignmentRepository.findByCollaborateur(collab).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AssignmentResponse updateAssignment(Long assignmentId, AssignmentRequest request) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Affectation non trouvee"));

        User collaborateur = userRepository.findById(request.collaborateurId())
                .orElseThrow(() -> new EntityNotFoundException("Collaborateur non trouve"));

        Mission mission = missionRepository.findById(request.missionId())
                .orElseThrow(() -> new EntityNotFoundException("Mission non trouvee"));

        // Force mission dates
        LocalDate start = mission.getDateDebut();
        LocalDate end = mission.getDateFin();
        
        validatePeriod(start, end);

        assignment.setCollaborateur(collaborateur);
        assignment.setMission(mission);
        assignment.setDateDebut(start);
        assignment.setDateFin(end);

        return mapToResponse(assignmentRepository.save(assignment));
    }

    @Transactional
    public void deleteAssignment(Long assignmentId) {
        if (!assignmentRepository.existsById(assignmentId)) {
            throw new EntityNotFoundException("Affectation non trouvee");
        }
        assignmentRepository.deleteById(assignmentId);
    }

    private AssignmentResponse mapToResponse(Assignment a) {
        return new AssignmentResponse(
                a.getId(),
                a.getCollaborateur().getId(),
                a.getCollaborateur().getFirstName() + " " + a.getCollaborateur().getLastName(),
                a.getMission().getId(),
                a.getMission().getTitre(),
                a.getDateDebut(),
                a.getDateFin()
        );
    }

    private void validatePeriod(LocalDate start, LocalDate end) {
        if (start == null) {
            throw new IllegalArgumentException("Date de debut obligatoire");
        }
        if (end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("La date de fin ne peut pas etre avant la date de debut");
        }
    }
}
