package com.tarkshastra.app.repository;

import com.tarkshastra.app.entity.User;
import com.tarkshastra.app.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.institute.id = :instituteId AND r = :role")
    Page<User> findByInstituteIdAndRole(@Param("instituteId") UUID instituteId, @Param("role") Role role, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.institute.id = :instituteId AND r = :role")
    List<User> findAllByInstituteIdAndRole(@Param("instituteId") UUID instituteId, @Param("role") Role role);

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE u.institute.id = :instituteId AND r = :role")
    long countByInstituteIdAndRole(@Param("instituteId") UUID instituteId, @Param("role") Role role);

    Optional<User> findByEmailAndInstitute_Id(String email, UUID instituteId);
}
