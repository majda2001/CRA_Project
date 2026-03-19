package com.example.crabackend.repository;

import com.example.crabackend.model.Assignment;
import com.example.crabackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByCollaborateur(User collaborateur);
       List<Assignment> findByMission_Id(Long missionId);

    @Query("SELECT a FROM Assignment a WHERE a.collaborateur.id = :userId AND " +
           "(a.dateFin IS NULL OR a.dateFin >= :startOfMonth) AND " +
           "a.dateDebut <= :endOfMonth")
    List<Assignment> findActiveAssignmentsForMonth(@Param("userId") Long userId,
                                                   @Param("startOfMonth") LocalDate startOfMonth,
                                                   @Param("endOfMonth") LocalDate endOfMonth);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Assignment a " +
           "WHERE a.collaborateur.id = :userId AND a.mission.id = :missionId " +
           "AND a.dateDebut <= :targetDate AND (a.dateFin IS NULL OR a.dateFin >= :targetDate)")
    boolean existsActiveAssignmentForDate(@Param("userId") Long userId,
                                          @Param("missionId") Long missionId,
                                          @Param("targetDate") LocalDate targetDate);

    @Query("SELECT a FROM Assignment a WHERE a.collaborateur.id = :userId " +
           "AND a.dateDebut <= :targetDate AND (a.dateFin IS NULL OR a.dateFin >= :targetDate)")
    List<Assignment> findActiveAssignmentsForDate(@Param("userId") Long userId,
                                                  @Param("targetDate") LocalDate targetDate);
}
