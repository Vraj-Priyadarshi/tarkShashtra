package com.tarkshastra.app.repository;

import com.tarkshastra.app.entity.Institute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstituteRepository extends JpaRepository<Institute, UUID> {

    Optional<Institute> findByAisheCode(String aisheCode);

    boolean existsByAisheCode(String aisheCode);
}
