package com.tarkshastra.app.repository;

import com.tarkshastra.app.entity.ConsistencyStreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConsistencyStreakRepository extends JpaRepository<ConsistencyStreak, UUID> {

    Optional<ConsistencyStreak> findByStudentId(UUID studentId);
}
