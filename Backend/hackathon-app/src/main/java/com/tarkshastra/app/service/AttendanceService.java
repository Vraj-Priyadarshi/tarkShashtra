package com.tarkshastra.app.service;

import com.tarkshastra.app.dto.request.AttendanceSessionRequest;
import com.tarkshastra.app.entity.*;
import com.tarkshastra.app.enums.AttendanceEntryMode;
import com.tarkshastra.app.enums.AttendanceStatus;
import com.tarkshastra.app.exception.BadRequestException;
import com.tarkshastra.app.exception.ResourceNotFoundException;
import com.tarkshastra.app.repository.AttendanceRecordRepository;
import com.tarkshastra.app.repository.AttendanceSessionRepository;
import com.tarkshastra.app.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final SubjectService subjectService;
    private final ClassService classService;

    @Transactional
    public AttendanceSession createAttendanceSession(AttendanceSessionRequest request, User teacher) {
        Subject subject = subjectService.getSubjectById(request.getSubjectId());
        ClassEntity classEntity = classService.getClassById(request.getClassId());

        AttendanceSession session = AttendanceSession.builder()
                .subject(subject)
                .classEntity(classEntity)
                .teacher(teacher)
                .sessionDate(request.getSessionDate())
                .entryMode(request.getEntryMode())
                .records(new ArrayList<>())
                .build();

        session = attendanceSessionRepository.save(session);

        // Create records for each student
        for (AttendanceSessionRequest.AttendanceRecordRequest rec : request.getRecords()) {
            User student = getStudentUser(rec.getStudentId());

            AttendanceRecord record = AttendanceRecord.builder()
                    .attendanceSession(session)
                    .student(student)
                    .build();

            if (request.getEntryMode() == AttendanceEntryMode.PER_SESSION) {
                if (rec.getStatus() == null) {
                    throw new BadRequestException("Status required for PER_SESSION mode");
                }
                record.setStatus(rec.getStatus());
            } else {
                if (rec.getBulkPercentage() == null) {
                    throw new BadRequestException("Bulk percentage required for BULK_PERCENTAGE mode");
                }
                record.setBulkPercentage(rec.getBulkPercentage());
            }

            session.getRecords().add(attendanceRecordRepository.save(record));
        }

        return session;
    }

    public List<AttendanceSession> getSessionsBySubjectAndClass(UUID subjectId, UUID classId) {
        return attendanceSessionRepository.findBySubjectIdAndClassEntityId(subjectId, classId);
    }

    public Double getAttendancePercentage(UUID studentId, UUID subjectId) {
        long present = attendanceRecordRepository.countByStudentIdAndSubjectIdAndStatus(
                studentId, subjectId, AttendanceStatus.PRESENT);
        long total = attendanceRecordRepository.countByStudentIdAndSubjectId(studentId, subjectId);

        if (total == 0) return null;
        return (present * 100.0) / total;
    }

    private User getStudentUser(UUID studentId) {
        StudentProfile sp = studentProfileRepository.findByUserId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
        return sp.getUser();
    }
}
