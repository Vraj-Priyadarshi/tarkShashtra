package com.tarkshastra.app.config;

import com.tarkshastra.app.entity.*;
import com.tarkshastra.app.enums.Role;
import com.tarkshastra.app.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DataSeeder — Seeds institutes and their academic coordinators on first startup.
 * LDCE (U-0003) is the primary institute with full academic structure.
 *
 * Seeded Institutes & Coordinator Credentials:
 * ─────────────────────────────────────────────────────────────────────────────
 * 1. Pandit Deendayal Energy University (AISHE: U-0001)
 *    Coordinator email   : coordinator@pdeu.ac.in
 *    Temporary password  : PDEU@Coord2026
 *
 * 2. Nirma University (AISHE: U-0002)
 *    Coordinator email   : coordinator@nirmauni.ac.in
 *    Temporary password  : Nirma@Coord2026
 *
 * 3. LD College of Engineering (AISHE: U-0003)  ← PRIMARY
 *    Coordinator email   : coordinator@ldce.ac.in
 *    Temporary password  : LDCE@Coord2026
 *    Academic structure  : 3 depts, 4 classes, 13 subjects, all mappings
 * ─────────────────────────────────────────────────────────────────────────────
 * All coordinators must change their password on first login.
 * Seeding is skipped if the AISHE code already exists in the database.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private final InstituteRepository instituteRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentRepository departmentRepository;
    private final ClassEntityRepository classEntityRepository;
    private final SubjectRepository subjectRepository;
    private final SubjectClassMappingRepository subjectClassMappingRepository;

    private record SeedEntry(String aisheCode, String instituteName,
                             String coordinatorEmail, String tempPassword) {}

    private static final List<SeedEntry> SEED_DATA = List.of(
            new SeedEntry(
                    "U-0001",
                    "Pandit Deendayal Energy University",
                    "coordinator@pdeu.ac.in",
                    "PDEU@Coord2026"
            ),
            new SeedEntry(
                    "U-0002",
                    "Nirma University",
                    "coordinator@nirmauni.ac.in",
                    "Nirma@Coord2026"
            ),
            new SeedEntry(
                    "U-0003",
                    "LD College of Engineering",
                    "coordinator@ldce.ac.in",
                    "LDCE@Coord2026"
            )
    );

    @Override
    @Transactional
    public void run(String... args) {
        for (SeedEntry entry : SEED_DATA) {
            Institute institute;
            if (instituteRepository.existsByAisheCode(entry.aisheCode())) {
                log.info("[DataSeeder] Institute already exists: {} ({})",
                        entry.instituteName(), entry.aisheCode());
                institute = instituteRepository.findByAisheCode(entry.aisheCode()).orElse(null);
            } else {
                institute = instituteRepository.save(
                        Institute.builder()
                                .aisheCode(entry.aisheCode())
                                .name(entry.instituteName())
                                .build()
                );
                log.info("[DataSeeder] Seeded institute: {} ({})", institute.getName(), institute.getAisheCode());
            }

            if (institute == null) continue;

            if (!userRepository.existsByEmail(entry.coordinatorEmail())) {
                User coordinator = User.builder()
                        .email(entry.coordinatorEmail().toLowerCase())
                        .passwordHash(passwordEncoder.encode(entry.tempPassword()))
                        .roles(Set.of(Role.ACADEMIC_COORDINATOR))
                        .institute(institute)
                        .mustChangePassword(true)
                        .isActive(true)
                        .build();
                userRepository.save(coordinator);
                log.info("[DataSeeder] Seeded coordinator: {} (password must be changed on first login)",
                        entry.coordinatorEmail());
            }

            // Always ensure LDCE academic structure exists (idempotent — skips if already present)
            if ("U-0003".equals(entry.aisheCode())) {
                seedLdceAcademicStructure(institute);
            }
        }
    }

    // ── LDCE Academic Structure ──────────────────────────────────────────────
    private static final String ACADEMIC_YEAR = "2025-26";

    private void seedLdceAcademicStructure(Institute institute) {
        log.info("[DataSeeder] Seeding LDCE academic structure (departments, classes, subjects, mappings)...");

        // --- Departments ---
        Department ce = seedDepartment(institute, "Computer Engineering", "CE");
        Department me = seedDepartment(institute, "Mechanical Engineering", "ME");
        Department ec = seedDepartment(institute, "Electronics & Communication Engineering", "EC");

        // --- Classes ---
        ClassEntity ceA = seedClass(institute, ce, "CE-A", 3);
        ClassEntity ceB = seedClass(institute, ce, "CE-B", 3);
        ClassEntity meA = seedClass(institute, me, "ME-A", 3);
        ClassEntity ecA = seedClass(institute, ec, "EC-A", 3);

        // --- Subjects ---
        Subject ce301 = seedSubject(institute, ce, "CE301", "Data Structures");
        Subject ce302 = seedSubject(institute, ce, "CE302", "Object Oriented Programming");
        Subject ce303 = seedSubject(institute, ce, "CE303", "Database Management Systems");
        Subject ce304 = seedSubject(institute, ce, "CE304", "Operating Systems");
        Subject ce305 = seedSubject(institute, ce, "CE305", "Discrete Mathematics");

        Subject me301 = seedSubject(institute, me, "ME301", "Fluid Mechanics");
        Subject me302 = seedSubject(institute, me, "ME302", "Thermodynamics");
        Subject me303 = seedSubject(institute, me, "ME303", "Strength of Materials");
        Subject me304 = seedSubject(institute, me, "ME304", "Manufacturing Technology");

        Subject ec301 = seedSubject(institute, ec, "EC301", "Signals and Systems");
        Subject ec302 = seedSubject(institute, ec, "EC302", "Digital Electronics");
        Subject ec303 = seedSubject(institute, ec, "EC303", "Analog Circuits");
        Subject ec304 = seedSubject(institute, ec, "EC304", "Electromagnetic Theory");

        // --- Subject-Class Mappings ---
        // CE subjects → CE-A and CE-B
        for (Subject s : List.of(ce301, ce302, ce303, ce304, ce305)) {
            seedMapping(s, ceA, 3);
            seedMapping(s, ceB, 3);
        }
        // ME subjects → ME-A
        for (Subject s : List.of(me301, me302, me303, me304)) {
            seedMapping(s, meA, 3);
        }
        // EC subjects → EC-A
        for (Subject s : List.of(ec301, ec302, ec303, ec304)) {
            seedMapping(s, ecA, 3);
        }

        log.info("[DataSeeder] LDCE academic structure seeded successfully.");
    }

    private Department seedDepartment(Institute institute, String name, String code) {
        return departmentRepository.findByInstituteIdAndCode(institute.getId(), code)
                .orElseGet(() -> {
                    Department d = departmentRepository.save(Department.builder()
                            .name(name).code(code).institute(institute).build());
                    log.info("[DataSeeder]   Department: {} ({})", name, code);
                    return d;
                });
    }

    private ClassEntity seedClass(Institute institute, Department dept, String name, int semester) {
        return classEntityRepository
                .findByDepartmentIdAndNameAndAcademicYear(dept.getId(), name, ACADEMIC_YEAR)
                .orElseGet(() -> {
                    ClassEntity c = classEntityRepository.save(ClassEntity.builder()
                            .name(name).semester(semester).academicYear(ACADEMIC_YEAR)
                            .department(dept).institute(institute).build());
                    log.info("[DataSeeder]   Class: {} (sem {}, {})", name, semester, ACADEMIC_YEAR);
                    return c;
                });
    }

    private Subject seedSubject(Institute institute, Department dept, String code, String name) {
        return subjectRepository.findByInstituteIdAndCode(institute.getId(), code)
                .orElseGet(() -> {
                    Subject s = subjectRepository.save(Subject.builder()
                            .name(name).code(code).department(dept).institute(institute).build());
                    log.info("[DataSeeder]   Subject: {} — {}", code, name);
                    return s;
                });
    }

    private void seedMapping(Subject subject, ClassEntity classEntity, int semester) {
        if (!subjectClassMappingRepository.existsBySubjectIdAndClassEntityIdAndAcademicYear(
                subject.getId(), classEntity.getId(), ACADEMIC_YEAR)) {
            subjectClassMappingRepository.save(SubjectClassMapping.builder()
                    .subject(subject).classEntity(classEntity)
                    .semester(semester).academicYear(ACADEMIC_YEAR).build());
        }
    }
}
