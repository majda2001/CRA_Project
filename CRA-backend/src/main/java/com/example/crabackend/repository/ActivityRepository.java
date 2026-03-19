package com.example.crabackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.crabackend.model.Activity;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
}
