package com.example.crabackend.repository;

import com.example.crabackend.model.Cra;
import com.example.crabackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CraRepository extends JpaRepository<Cra, Long> {
    Optional<Cra> findByCollaborateurAndMoisAndAnnee(User collaborateur, Integer mois, Integer annee);
}
