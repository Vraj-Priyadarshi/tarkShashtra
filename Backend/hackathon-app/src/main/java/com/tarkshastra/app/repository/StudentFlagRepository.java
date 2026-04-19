package com.tarkshastra.app.repository;

import com.tarkshastra.app.entity.StudentFlag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudentFlagRepository extends JpaRepository<StudentFlag, UUID> {

    List<StudentFlag> findByStudentIdAndIsResolvedFalse(UUID studentId);

    @Query("SELECT sf FROM StudentFlag sf JOIN StudentProfile sp ON sp.user.id = sf.student.id WHERE sp.mentor.id = :mentorId AND sf.isResolved = false")
    List<StudentFlag> findUnresolvedByMentorId(@Param("mentorId") UUID mentorId);

    Page<StudentFlag> findByFlaggedById(UUID teacherId, Pageable pageable);
}
