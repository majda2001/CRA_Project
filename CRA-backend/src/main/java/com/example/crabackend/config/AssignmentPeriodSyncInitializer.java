package com.example.crabackend.config;

import com.example.crabackend.model.Assignment;
import com.example.crabackend.model.Mission;
import com.example.crabackend.repository.AssignmentRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssignmentPeriodSyncInitializer {

    private final AssignmentRepository assignmentRepository;

    @PostConstruct
    @Transactional
    public void syncAssignmentPeriodsWithMission() {
        List<Assignment> assignments = assignmentRepository.findAll();
        int updated = 0;

        for (Assignment assignment : assignments) {
            Mission mission = assignment.getMission();
            if (mission == null) {
                continue;
            }

            boolean needsUpdate =
                    assignment.getDateDebut() == null
                            || !assignment.getDateDebut().equals(mission.getDateDebut())
                            || (assignment.getDateFin() == null
                                    ? mission.getDateFin() != null
                                    : !assignment.getDateFin().equals(mission.getDateFin()));

            if (!needsUpdate) {
                continue;
            }

            assignment.setDateDebut(mission.getDateDebut());
            assignment.setDateFin(mission.getDateFin());
            updated++;
        }

        if (updated > 0) {
            assignmentRepository.saveAll(assignments);
            log.info("Synchronisation affectations terminee: {} affectation(s) alignee(s) sur les periodes de mission.", updated);
        } else {
            log.info("Synchronisation affectations: aucune mise a jour necessaire.");
        }
    }
}
