package com.example.crabackend.service;

import com.example.crabackend.dto.MissionRequest;
import com.example.crabackend.dto.MissionResponse;
import com.example.crabackend.model.Assignment;
import com.example.crabackend.model.Mission;
import com.example.crabackend.repository.AssignmentRepository;
import com.example.crabackend.repository.MissionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository repository;
    private final AssignmentRepository assignmentRepository;

    @Transactional(readOnly = true)
    public List<MissionResponse> getAllMissions() {
        return repository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MissionResponse getMissionById(Long id) {
        return repository.findById(id).map(this::mapToResponse)
                .orElseThrow(() -> new EntityNotFoundException("Mission non trouvee"));
    }

    @Transactional
    public MissionResponse createMission(MissionRequest request) {
        Mission mission = Mission.builder()
                .titre(request.titre())
                .description(request.description())
                .dateDebut(request.dateDebut())
                .dateFin(request.dateFin())
                .motif(request.motif())
                .tjm(request.tjm())
                .build();
        return mapToResponse(repository.save(mission));
    }

    @Transactional
    public MissionResponse updateMission(Long id, MissionRequest request) {
        Mission existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mission non trouvee"));

        existing.setTitre(request.titre());
        existing.setDescription(request.description());
        existing.setDateDebut(request.dateDebut());
        existing.setDateFin(request.dateFin());
        existing.setMotif(request.motif());
        existing.setTjm(request.tjm());

        List<Assignment> linkedAssignments = assignmentRepository.findByMission_Id(existing.getId());
        for (Assignment assignment : linkedAssignments) {
            assignment.setDateDebut(existing.getDateDebut());
            assignment.setDateFin(existing.getDateFin());
        }
        if (!linkedAssignments.isEmpty()) {
            assignmentRepository.saveAll(linkedAssignments);
        }

        return mapToResponse(repository.save(existing));
    }

    @Transactional
    public void deleteMission(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Mission non trouvee");
        }
        repository.deleteById(id);
    }

    private MissionResponse mapToResponse(Mission mission) {
        return new MissionResponse(
                mission.getId(),
                mission.getTitre(),
                mission.getDescription(),
                mission.getDateDebut(),
                mission.getDateFin(),
                mission.getMotif(),
                mission.getTjm()
        );
    }
}
