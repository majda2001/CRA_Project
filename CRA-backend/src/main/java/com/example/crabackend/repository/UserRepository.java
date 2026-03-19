package com.example.crabackend.repository;

import com.example.crabackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
import com.example.crabackend.model.Role;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByActivationToken(String activationToken);
    boolean existsByEmail(String email);
    boolean existsByRole(Role role);
    List<User> findAllByRole(Role role);
}
