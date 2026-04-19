package com.tarkshastra.app.config;

import com.tarkshastra.app.entity.*;
import com.tarkshastra.app.enums.*;
import com.tarkshastra.app.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * SampleDataSeeder — Seeds rich sample data for LDCE (U-0003) for demo/ML purposes.
 *
 * Creates: 66 students, 10 teachers, attendance (20 sessions/subject-class),
 *          IA marks (3 rounds), 3 assignments/subject-class, LMS scores.
 *
 * Test Accounts (password: Test@1234, mustChangePassword=false):
 *   Teacher : arun.kumar@ldce.ac.in   (SUBJECT_TEACHER + FACULTY_MENTOR)
 *   Student : aarav.patel@ldce.ac.in   (STUDENT)
 *
 * Idempotent — skips if students already exist in LDCE.
 */
// DISABLED — sample data seeder. CSV upload is preferred for manual testing.
// @Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class SampleDataSeeder implements CommandLineRunner {

    private final InstituteRepository instituteRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentRepository departmentRepository;
    private final ClassEntityRepository classEntityRepository;
    private final SubjectRepository subjectRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final SubjectTeacherMappingRepository subjectTeacherMappingRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final IAMarksRepository iaMarksRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final LMSScoreRepository lmsScoreRepository;

    private static final String ACADEMIC_YEAR = "2025-26";
    private static final String TEST_PASSWORD = "Test@1234";
    private final Random rng = new Random(42); // deterministic seed for reproducibility

    // ── Student definitions: {rollNumber, fullName, email} ──
    private static final String[][] CE_A_STUDENTS = {
        {"LDCE-CE-001", "Aarav Patel",       "aarav.patel@ldce.ac.in"},
        {"LDCE-CE-002", "Priya Sharma",      "priya.sharma@ldce.ac.in"},
        {"LDCE-CE-003", "Vivaan Modi",       "vivaan.modi@ldce.ac.in"},
        {"LDCE-CE-004", "Ananya Desai",      "ananya.desai@ldce.ac.in"},
        {"LDCE-CE-005", "Reyansh Mehta",     "reyansh.mehta@ldce.ac.in"},
        {"LDCE-CE-006", "Diya Shah",         "diya.shah@ldce.ac.in"},
        {"LDCE-CE-007", "Aditya Joshi",      "aditya.joshi@ldce.ac.in"},
        {"LDCE-CE-008", "Ishita Trivedi",    "ishita.trivedi@ldce.ac.in"},
        {"LDCE-CE-009", "Arjun Kumar",       "arjun.kumar@ldce.ac.in"},
        {"LDCE-CE-010", "Kavya Bhatt",       "kavya.bhatt@ldce.ac.in"},
        {"LDCE-CE-011", "Rohan Pandya",      "rohan.pandya@ldce.ac.in"},
        {"LDCE-CE-012", "Saanvi Chauhan",    "saanvi.chauhan@ldce.ac.in"},
        {"LDCE-CE-013", "Vihaan Rao",        "vihaan.rao@ldce.ac.in"},
        {"LDCE-CE-014", "Nisha Thakur",      "nisha.thakur@ldce.ac.in"},
        {"LDCE-CE-015", "Dhruv Agarwal",     "dhruv.agarwal@ldce.ac.in"},
        {"LDCE-CE-016", "Meera Iyer",        "meera.iyer@ldce.ac.in"},
        {"LDCE-CE-017", "Kabir Verma",       "kabir.verma@ldce.ac.in"},
        {"LDCE-CE-018", "Riya Nair",         "riya.nair@ldce.ac.in"},
    };

    private static final String[][] CE_B_STUDENTS = {
        {"LDCE-CE-019", "Advait Gupta",      "advait.gupta@ldce.ac.in"},
        {"LDCE-CE-020", "Shreya Reddy",      "shreya.reddy@ldce.ac.in"},
        {"LDCE-CE-021", "Ayaan Singh",       "ayaan.singh@ldce.ac.in"},
        {"LDCE-CE-022", "Tara Menon",        "tara.menon@ldce.ac.in"},
        {"LDCE-CE-023", "Siddharth Kapoor",  "siddharth.kapoor@ldce.ac.in"},
        {"LDCE-CE-024", "Aisha Khan",        "aisha.khan@ldce.ac.in"},
        {"LDCE-CE-025", "Arnav Soni",        "arnav.soni@ldce.ac.in"},
        {"LDCE-CE-026", "Zara Patel",        "zara.patel@ldce.ac.in"},
        {"LDCE-CE-027", "Yash Parikh",       "yash.parikh@ldce.ac.in"},
        {"LDCE-CE-028", "Kiara Dave",        "kiara.dave@ldce.ac.in"},
        {"LDCE-CE-029", "Dev Choudhary",     "dev.choudhary@ldce.ac.in"},
        {"LDCE-CE-030", "Myra Saxena",       "myra.saxena@ldce.ac.in"},
        {"LDCE-CE-031", "Krish Bhatia",      "krish.bhatia@ldce.ac.in"},
        {"LDCE-CE-032", "Avni Tiwari",       "avni.tiwari@ldce.ac.in"},
        {"LDCE-CE-033", "Rudra Mishra",      "rudra.mishra@ldce.ac.in"},
        {"LDCE-CE-034", "Sia Kulkarni",      "sia.kulkarni@ldce.ac.in"},
        {"LDCE-CE-035", "Lakshay Yadav",     "lakshay.yadav@ldce.ac.in"},
        {"LDCE-CE-036", "Pooja Hegde",       "pooja.hegde@ldce.ac.in"},
    };

    private static final String[][] ME_A_STUDENTS = {
        {"LDCE-ME-001", "Harsh Raval",       "harsh.raval@ldce.ac.in"},
        {"LDCE-ME-002", "Sneha Patel",       "sneha.patel@ldce.ac.in"},
        {"LDCE-ME-003", "Raj Kothari",       "raj.kothari@ldce.ac.in"},
        {"LDCE-ME-004", "Neha Barot",        "neha.barot@ldce.ac.in"},
        {"LDCE-ME-005", "Karan Vyas",        "karan.vyas@ldce.ac.in"},
        {"LDCE-ME-006", "Ankita Prajapati",  "ankita.prajapati@ldce.ac.in"},
        {"LDCE-ME-007", "Mihir Solanki",     "mihir.solanki@ldce.ac.in"},
        {"LDCE-ME-008", "Tanvi Gajjar",      "tanvi.gajjar@ldce.ac.in"},
        {"LDCE-ME-009", "Jayesh Mistry",     "jayesh.mistry@ldce.ac.in"},
        {"LDCE-ME-010", "Roshni Darji",      "roshni.darji@ldce.ac.in"},
        {"LDCE-ME-011", "Parth Dhami",       "parth.dhami@ldce.ac.in"},
        {"LDCE-ME-012", "Khushi Panchal",    "khushi.panchal@ldce.ac.in"},
        {"LDCE-ME-013", "Nirav Thakor",      "nirav.thakor@ldce.ac.in"},
        {"LDCE-ME-014", "Heena Makwana",     "heena.makwana@ldce.ac.in"},
        {"LDCE-ME-015", "Chirag Rathod",     "chirag.rathod@ldce.ac.in"},
    };

    private static final String[][] EC_A_STUDENTS = {
        {"LDCE-EC-001", "Manav Sanghvi",     "manav.sanghvi@ldce.ac.in"},
        {"LDCE-EC-002", "Drashti Contractor","drashti.contractor@ldce.ac.in"},
        {"LDCE-EC-003", "Yuvraj Suthar",     "yuvraj.suthar@ldce.ac.in"},
        {"LDCE-EC-004", "Jhanvi Dalal",      "jhanvi.dalal@ldce.ac.in"},
        {"LDCE-EC-005", "Darshan Thaker",    "darshan.thaker@ldce.ac.in"},
        {"LDCE-EC-006", "Rinku Vaghela",     "rinku.vaghela@ldce.ac.in"},
        {"LDCE-EC-007", "Vatsal Amin",       "vatsal.amin@ldce.ac.in"},
        {"LDCE-EC-008", "Bhavika Chaudhary", "bhavika.chaudhary@ldce.ac.in"},
        {"LDCE-EC-009", "Tushar Jadav",      "tushar.jadav@ldce.ac.in"},
        {"LDCE-EC-010", "Nidhi Bhavsar",     "nidhi.bhavsar@ldce.ac.in"},
        {"LDCE-EC-011", "Gaurav Chavda",     "gaurav.chavda@ldce.ac.in"},
        {"LDCE-EC-012", "Sonal Parmar",      "sonal.parmar@ldce.ac.in"},
        {"LDCE-EC-013", "Jaymin Gohel",      "jaymin.gohel@ldce.ac.in"},
        {"LDCE-EC-014", "Vrinda Doshi",      "vrinda.doshi@ldce.ac.in"},
        {"LDCE-EC-015", "Rahul Limbachiya",  "rahul.limbachiya@ldce.ac.in"},
    };

    // ── Teacher definitions: {employeeId, fullName, email, deptCode} ──
    private static final String[][] TEACHERS = {
        {"T001", "Dr. Rajesh Patel",    "rajesh.patel@ldce.ac.in",    "CE"},
        {"T002", "Prof. Meena Shah",    "meena.shah@ldce.ac.in",      "CE"},
        {"T003", "Dr. Vikram Desai",    "vikram.desai@ldce.ac.in",    "CE"},
        {"T004", "Prof. Anita Sharma",  "anita.sharma@ldce.ac.in",    "CE"},
        {"T005", "Dr. Suresh Kumar",    "suresh.kumar@ldce.ac.in",    "CE"},
        {"T006", "Dr. Priya Mehta",     "priya.mehta@ldce.ac.in",     "ME"},
        {"T007", "Prof. Ramesh Joshi",  "ramesh.joshi@ldce.ac.in",    "ME"},
        {"T008", "Dr. Kavita Trivedi",  "kavita.trivedi@ldce.ac.in",  "ME"},
        {"T009", "Dr. Amit Bhatt",      "amit.bhatt@ldce.ac.in",      "EC"},
        {"T010", "Prof. Neha Pandya",   "neha.pandya@ldce.ac.in",     "EC"},
    };

    // Subject→teacher mappings: {teacherEmail, subjectCode, className}
    private static final String[][] SUBJECT_TEACHER_MAP = {
        {"rajesh.patel@ldce.ac.in",   "CE301", "CE-A"},
        {"rajesh.patel@ldce.ac.in",   "CE301", "CE-B"},
        {"meena.shah@ldce.ac.in",     "CE302", "CE-A"},
        {"meena.shah@ldce.ac.in",     "CE302", "CE-B"},
        {"vikram.desai@ldce.ac.in",   "CE303", "CE-A"},
        {"vikram.desai@ldce.ac.in",   "CE303", "CE-B"},
        {"anita.sharma@ldce.ac.in",   "CE304", "CE-A"},
        {"anita.sharma@ldce.ac.in",   "CE304", "CE-B"},
        {"suresh.kumar@ldce.ac.in",   "CE305", "CE-A"},
        {"suresh.kumar@ldce.ac.in",   "CE305", "CE-B"},
        {"priya.mehta@ldce.ac.in",    "ME301", "ME-A"},
        {"priya.mehta@ldce.ac.in",    "ME302", "ME-A"},
        {"ramesh.joshi@ldce.ac.in",   "ME303", "ME-A"},
        {"ramesh.joshi@ldce.ac.in",   "ME304", "ME-A"},
        {"kavita.trivedi@ldce.ac.in", "ME301", "ME-A"},
        {"amit.bhatt@ldce.ac.in",     "EC301", "EC-A"},
        {"amit.bhatt@ldce.ac.in",     "EC302", "EC-A"},
        {"neha.pandya@ldce.ac.in",    "EC303", "EC-A"},
        {"neha.pandya@ldce.ac.in",    "EC304", "EC-A"},
    };

    // Test account (teacher) that also appears in TEACHERS array
    private static final String TEST_TEACHER_EMAIL = "arun.kumar@ldce.ac.in";
    // Test account (student) that also appears in CE_A_STUDENTS array
    private static final String TEST_STUDENT_EMAIL = "aarav.patel@ldce.ac.in";

    @Override
    @Transactional
    public void run(String... args) {
        Institute ldce = instituteRepository.findByAisheCode("U-0003").orElse(null);
        if (ldce == null) {
            log.warn("[SampleDataSeeder] LDCE institute not found, skipping.");
            return;
        }

        if (studentProfileRepository.countByInstituteId(ldce.getId()) > 0) {
            log.info("[SampleDataSeeder] Students already exist in LDCE, skipping.");
            return;
        }

        log.info("[SampleDataSeeder] Seeding sample data for LDCE...");

        // Lookup departments
        Department ce = departmentRepository.findByInstituteIdAndCode(ldce.getId(), "CE").orElse(null);
        Department me = departmentRepository.findByInstituteIdAndCode(ldce.getId(), "ME").orElse(null);
        Department ec = departmentRepository.findByInstituteIdAndCode(ldce.getId(), "EC").orElse(null);
        if (ce == null || me == null || ec == null) {
            log.warn("[SampleDataSeeder] Departments not found, skipping.");
            return;
        }

        // Lookup classes
        Map<String, ClassEntity> classMap = new HashMap<>();
        for (String name : List.of("CE-A", "CE-B")) {
            classEntityRepository.findByDepartmentIdAndNameAndAcademicYear(ce.getId(), name, ACADEMIC_YEAR)
                    .ifPresent(c -> classMap.put(name, c));
        }
        classEntityRepository.findByDepartmentIdAndNameAndAcademicYear(me.getId(), "ME-A", ACADEMIC_YEAR)
                .ifPresent(c -> classMap.put("ME-A", c));
        classEntityRepository.findByDepartmentIdAndNameAndAcademicYear(ec.getId(), "EC-A", ACADEMIC_YEAR)
                .ifPresent(c -> classMap.put("EC-A", c));

        if (classMap.size() < 4) {
            log.warn("[SampleDataSeeder] Not all classes found ({}), skipping.", classMap.size());
            return;
        }

        // Lookup subjects
        Map<String, Subject> subjectMap = new HashMap<>();
        for (String code : List.of("CE301","CE302","CE303","CE304","CE305",
                                   "ME301","ME302","ME303","ME304",
                                   "EC301","EC302","EC303","EC304")) {
            subjectRepository.findByInstituteIdAndCode(ldce.getId(), code)
                    .ifPresent(s -> subjectMap.put(code, s));
        }

        Map<String, Department> deptMap = Map.of("CE", ce, "ME", me, "EC", ec);

        // ── 1. Seed teachers ──
        // Add test teacher first (special: known password, dual role)
        User testTeacher = seedTestTeacher(ldce, ce);

        Map<String, User> teacherUserMap = new HashMap<>();
        teacherUserMap.put(TEST_TEACHER_EMAIL, testTeacher);

        for (String[] t : TEACHERS) {
            String email = t[2];
            if (userRepository.existsByEmail(email)) {
                userRepository.findByEmail(email).ifPresent(u -> teacherUserMap.put(email, u));
                continue;
            }
            Department dept = deptMap.get(t[3]);
            User user = User.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode(TEST_PASSWORD))
                    .roles(Set.of(Role.SUBJECT_TEACHER, Role.FACULTY_MENTOR))
                    .institute(ldce)
                    .fullName(t[1])
                    .mustChangePassword(false)
                    .isActive(true)
                    .build();
            user = userRepository.save(user);
            teacherUserMap.put(email, user);

            teacherProfileRepository.save(TeacherProfile.builder()
                    .user(user).fullName(t[1]).employeeId(t[0])
                    .department(dept).institute(ldce).build());
        }

        // Create subject-teacher mappings
        for (String[] stm : SUBJECT_TEACHER_MAP) {
            User teacher = teacherUserMap.get(stm[0]);
            Subject subject = subjectMap.get(stm[1]);
            ClassEntity cls = classMap.get(stm[2]);
            if (teacher == null || subject == null || cls == null) continue;

            if (!subjectTeacherMappingRepository.existsByTeacherIdAndSubjectIdAndClassEntityIdAndAcademicYear(
                    teacher.getId(), subject.getId(), cls.getId(), ACADEMIC_YEAR)) {
                subjectTeacherMappingRepository.save(SubjectTeacherMapping.builder()
                        .teacher(teacher).subject(subject).classEntity(cls)
                        .academicYear(ACADEMIC_YEAR).build());
            }
        }

        // Also map test teacher to CE301 CE-A and CE301 CE-B
        Subject ce301 = subjectMap.get("CE301");
        if (ce301 != null && testTeacher != null) {
            for (String cn : List.of("CE-A", "CE-B")) {
                ClassEntity cls = classMap.get(cn);
                if (cls != null && !subjectTeacherMappingRepository.existsByTeacherIdAndSubjectIdAndClassEntityIdAndAcademicYear(
                        testTeacher.getId(), ce301.getId(), cls.getId(), ACADEMIC_YEAR)) {
                    subjectTeacherMappingRepository.save(SubjectTeacherMapping.builder()
                            .teacher(testTeacher).subject(ce301).classEntity(cls)
                            .academicYear(ACADEMIC_YEAR).build());
                }
            }
        }

        log.info("[SampleDataSeeder] Seeded {} teachers with subject mappings.", teacherUserMap.size());

        // ── 2. Seed students ──
        // Mentor assignments: CE teachers mentor CE students, ME→ME, EC→EC
        List<User> ceMentors = List.of(
                teacherUserMap.get("rajesh.patel@ldce.ac.in"),
                teacherUserMap.get("meena.shah@ldce.ac.in"),
                teacherUserMap.get("vikram.desai@ldce.ac.in"));
        List<User> meMentors = List.of(
                teacherUserMap.get("priya.mehta@ldce.ac.in"),
                teacherUserMap.get("ramesh.joshi@ldce.ac.in"));
        List<User> ecMentors = List.of(
                teacherUserMap.get("amit.bhatt@ldce.ac.in"),
                teacherUserMap.get("neha.pandya@ldce.ac.in"));

        List<StudentProfile> allStudents = new ArrayList<>();
        allStudents.addAll(seedStudentBatch(ldce, ce, classMap.get("CE-A"), CE_A_STUDENTS, ceMentors));
        allStudents.addAll(seedStudentBatch(ldce, ce, classMap.get("CE-B"), CE_B_STUDENTS, ceMentors));
        allStudents.addAll(seedStudentBatch(ldce, me, classMap.get("ME-A"), ME_A_STUDENTS, meMentors));
        allStudents.addAll(seedStudentBatch(ldce, ec, classMap.get("EC-A"), EC_A_STUDENTS, ecMentors));

        log.info("[SampleDataSeeder] Seeded {} students.", allStudents.size());

        // ── 3. Assign performance profiles to students for varied ML data ──
        // Each student gets a "performance factor" (0.0 = worst, 1.0 = best)
        // Distribution: ~20% HIGH risk, ~30% MEDIUM risk, ~50% LOW risk
        Map<UUID, Double> performanceMap = new HashMap<>();
        for (int i = 0; i < allStudents.size(); i++) {
            double factor;
            double rand = rng.nextDouble();
            if (rand < 0.20) {
                factor = 0.25 + rng.nextDouble() * 0.20; // 0.25–0.45 (high risk)
            } else if (rand < 0.50) {
                factor = 0.50 + rng.nextDouble() * 0.20; // 0.50–0.70 (medium risk)
            } else {
                factor = 0.75 + rng.nextDouble() * 0.20; // 0.75–0.95 (low risk)
            }
            performanceMap.put(allStudents.get(i).getUser().getId(), factor);
        }

        // ── 4. Seed attendance ──
        seedAttendanceData(allStudents, subjectMap, classMap, teacherUserMap, performanceMap);

        // ── 5. Seed IA marks ──
        seedIAMarksData(allStudents, subjectMap, classMap, teacherUserMap, performanceMap);

        // ── 6. Seed assignments ──
        seedAssignmentData(allStudents, subjectMap, classMap, teacherUserMap, performanceMap);

        // ── 7. Seed LMS scores ──
        seedLMSScoreData(allStudents, subjectMap, classMap, teacherUserMap, performanceMap);

        log.info("[SampleDataSeeder] ✅ All sample data seeded successfully for LDCE!");
    }

    // ── Test teacher account ──
    private User seedTestTeacher(Institute institute, Department ce) {
        if (userRepository.existsByEmail(TEST_TEACHER_EMAIL)) {
            return userRepository.findByEmail(TEST_TEACHER_EMAIL).orElse(null);
        }
        User user = User.builder()
                .email(TEST_TEACHER_EMAIL)
                .passwordHash(passwordEncoder.encode(TEST_PASSWORD))
                .roles(Set.of(Role.SUBJECT_TEACHER, Role.FACULTY_MENTOR))
                .institute(institute)
                .fullName("Prof. Arun Kumar")
                .mustChangePassword(false)
                .isActive(true)
                .build();
        user = userRepository.save(user);

        teacherProfileRepository.save(TeacherProfile.builder()
                .user(user).fullName("Prof. Arun Kumar").employeeId("T000")
                .department(ce).institute(institute).build());

        log.info("[SampleDataSeeder] Test teacher: {} / {}", TEST_TEACHER_EMAIL, TEST_PASSWORD);
        return user;
    }

    // ── Seed a batch of students ──
    private List<StudentProfile> seedStudentBatch(Institute institute, Department dept,
                                                   ClassEntity classEntity, String[][] studentData,
                                                   List<User> mentors) {
        List<StudentProfile> profiles = new ArrayList<>();
        for (int i = 0; i < studentData.length; i++) {
            String[] s = studentData[i];
            String rollNumber = s[0];
            String fullName = s[1];
            String email = s[2];

            if (userRepository.existsByEmail(email)) {
                studentProfileRepository.findByRollNumberAndInstituteId(rollNumber, institute.getId())
                        .ifPresent(profiles::add);
                continue;
            }

            boolean isTestStudent = TEST_STUDENT_EMAIL.equals(email);
            User user = User.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode(TEST_PASSWORD))
                    .roles(Set.of(Role.STUDENT))
                    .institute(institute)
                    .fullName(fullName)
                    .mustChangePassword(!isTestStudent) // test student doesn't need to change
                    .isActive(true)
                    .build();
            user = userRepository.save(user);

            User mentor = mentors.get(i % mentors.size());
            StudentProfile profile = StudentProfile.builder()
                    .user(user).fullName(fullName).rollNumber(rollNumber)
                    .department(dept).classEntity(classEntity)
                    .semester(3).mentor(mentor).institute(institute)
                    .build();
            profile = studentProfileRepository.save(profile);
            profiles.add(profile);
        }
        if (!profiles.isEmpty()) {
            log.info("[SampleDataSeeder] Test student: {} / {}", TEST_STUDENT_EMAIL, TEST_PASSWORD);
        }
        return profiles;
    }

    // ── Get subjects applicable to a class ──
    private List<Subject> getSubjectsForClass(String className, Map<String, Subject> subjectMap) {
        if (className.startsWith("CE")) {
            return List.of(subjectMap.get("CE301"), subjectMap.get("CE302"),
                    subjectMap.get("CE303"), subjectMap.get("CE304"), subjectMap.get("CE305"));
        } else if (className.startsWith("ME")) {
            return List.of(subjectMap.get("ME301"), subjectMap.get("ME302"),
                    subjectMap.get("ME303"), subjectMap.get("ME304"));
        } else {
            return List.of(subjectMap.get("EC301"), subjectMap.get("EC302"),
                    subjectMap.get("EC303"), subjectMap.get("EC304"));
        }
    }

    // ── Find teacher for a subject+class ──
    private User findTeacher(String subjectCode, String className, Map<String, User> teacherMap) {
        for (String[] stm : SUBJECT_TEACHER_MAP) {
            if (stm[1].equals(subjectCode) && stm[2].equals(className)) {
                return teacherMap.get(stm[0]);
            }
        }
        return null;
    }

    // ──────────────────────────────────────────────────────────────────────
    // ATTENDANCE DATA: 20 sessions per subject-class, Aug 2025 → Mar 2026
    // ──────────────────────────────────────────────────────────────────────
    private void seedAttendanceData(List<StudentProfile> allStudents,
                                    Map<String, Subject> subjectMap,
                                    Map<String, ClassEntity> classMap,
                                    Map<String, User> teacherMap,
                                    Map<UUID, Double> performanceMap) {
        log.info("[SampleDataSeeder] Seeding attendance data...");
        int sessionCount = 0;

        for (Map.Entry<String, ClassEntity> ce : classMap.entrySet()) {
            String className = ce.getKey();
            ClassEntity classEntity = ce.getValue();
            List<Subject> subjects = getSubjectsForClass(className, subjectMap);
            List<StudentProfile> classStudents = allStudents.stream()
                    .filter(sp -> sp.getClassEntity().getId().equals(classEntity.getId()))
                    .toList();

            for (Subject subject : subjects) {
                if (subject == null) continue;
                User teacher = findTeacher(subject.getCode(), className, teacherMap);
                if (teacher == null) teacher = teacherMap.values().iterator().next();

                // Create 20 attendance sessions spread across Aug–Mar
                LocalDate startDate = LocalDate.of(2025, 8, 5);
                for (int sessionIdx = 0; sessionIdx < 20; sessionIdx++) {
                    LocalDate sessionDate = startDate.plusDays(sessionIdx * 10L); // every ~10 days

                    AttendanceSession session = AttendanceSession.builder()
                            .subject(subject)
                            .classEntity(classEntity)
                            .teacher(teacher)
                            .sessionDate(sessionDate)
                            .entryMode(AttendanceEntryMode.PER_SESSION)
                            .build();
                    session = attendanceSessionRepository.save(session);

                    List<AttendanceRecord> records = new ArrayList<>();
                    for (StudentProfile sp : classStudents) {
                        double factor = performanceMap.getOrDefault(sp.getUser().getId(), 0.7);
                        // Add some noise per session
                        double adjFactor = factor + (rng.nextDouble() - 0.5) * 0.2;
                        AttendanceStatus status = rng.nextDouble() < adjFactor
                                ? AttendanceStatus.PRESENT : AttendanceStatus.ABSENT;

                        records.add(AttendanceRecord.builder()
                                .attendanceSession(session)
                                .student(sp.getUser())
                                .status(status)
                                .build());
                    }
                    attendanceRecordRepository.saveAll(records);
                    sessionCount++;
                }
            }
        }
        log.info("[SampleDataSeeder] Seeded {} attendance sessions.", sessionCount);
    }

    // ──────────────────────────────────────────────────────────────────────
    // IA MARKS: 3 rounds, max=30, varied by performance factor
    // ──────────────────────────────────────────────────────────────────────
    private void seedIAMarksData(List<StudentProfile> allStudents,
                                  Map<String, Subject> subjectMap,
                                  Map<String, ClassEntity> classMap,
                                  Map<String, User> teacherMap,
                                  Map<UUID, Double> performanceMap) {
        log.info("[SampleDataSeeder] Seeding IA marks...");
        int count = 0;
        double maxMarks = 30.0;
        String[] rounds = {"IA-1", "IA-2", "IA-3"};

        for (Map.Entry<String, ClassEntity> ce : classMap.entrySet()) {
            String className = ce.getKey();
            ClassEntity classEntity = ce.getValue();
            List<Subject> subjects = getSubjectsForClass(className, subjectMap);
            List<StudentProfile> classStudents = allStudents.stream()
                    .filter(sp -> sp.getClassEntity().getId().equals(classEntity.getId()))
                    .toList();

            for (Subject subject : subjects) {
                if (subject == null) continue;
                User teacher = findTeacher(subject.getCode(), className, teacherMap);
                if (teacher == null) teacher = teacherMap.values().iterator().next();

                for (String round : rounds) {
                    List<IAMarks> marksList = new ArrayList<>();
                    for (StudentProfile sp : classStudents) {
                        double factor = performanceMap.getOrDefault(sp.getUser().getId(), 0.7);
                        // Add some progression (slightly better in later rounds for medium students)
                        double roundBonus = round.equals("IA-3") ? 0.05 : round.equals("IA-2") ? 0.02 : 0;
                        double adjFactor = Math.min(1.0, factor + roundBonus + (rng.nextDouble() - 0.5) * 0.15);

                        boolean absent = rng.nextDouble() > (factor + 0.15); // low performers more likely absent
                        double obtained = absent ? 0 : Math.round(adjFactor * maxMarks * 100.0) / 100.0;
                        obtained = Math.max(0, Math.min(maxMarks, obtained));
                        double normalized = obtained / maxMarks * 100.0;

                        marksList.add(IAMarks.builder()
                                .student(sp.getUser())
                                .subject(subject)
                                .classEntity(classEntity)
                                .teacher(teacher)
                                .iaRound(round)
                                .maxMarks(maxMarks)
                                .obtainedMarks(obtained)
                                .isAbsent(absent)
                                .normalizedScore(normalized)
                                .build());
                        count++;
                    }
                    iaMarksRepository.saveAll(marksList);
                }
            }
        }
        log.info("[SampleDataSeeder] Seeded {} IA marks entries.", count);
    }

    // ──────────────────────────────────────────────────────────────────────
    // ASSIGNMENTS: 3 per subject-class, with submission statuses
    // ──────────────────────────────────────────────────────────────────────
    private void seedAssignmentData(List<StudentProfile> allStudents,
                                     Map<String, Subject> subjectMap,
                                     Map<String, ClassEntity> classMap,
                                     Map<String, User> teacherMap,
                                     Map<UUID, Double> performanceMap) {
        log.info("[SampleDataSeeder] Seeding assignments...");
        int aCount = 0;
        String[][] assignmentTitles = {
                {"Assignment 1 – Basics", "Assignment 2 – Intermediate", "Assignment 3 – Advanced"},
        };

        for (Map.Entry<String, ClassEntity> ce : classMap.entrySet()) {
            String className = ce.getKey();
            ClassEntity classEntity = ce.getValue();
            List<Subject> subjects = getSubjectsForClass(className, subjectMap);
            List<StudentProfile> classStudents = allStudents.stream()
                    .filter(sp -> sp.getClassEntity().getId().equals(classEntity.getId()))
                    .toList();

            for (Subject subject : subjects) {
                if (subject == null) continue;
                User teacher = findTeacher(subject.getCode(), className, teacherMap);
                if (teacher == null) teacher = teacherMap.values().iterator().next();

                int[] assignmentMonths = {9, 11, 1};
                int[] assignmentYears  = {2025, 2025, 2026};
                for (int a = 0; a < 3; a++) {
                    LocalDate dueDate = LocalDate.of(assignmentYears[a], assignmentMonths[a], 15); // Sep, Nov, Jan

                    Assignment assignment = Assignment.builder()
                            .title(subject.getCode() + " – " + assignmentTitles[0][a])
                            .subject(subject)
                            .classEntity(classEntity)
                            .teacher(teacher)
                            .dueDate(dueDate)
                            .build();
                    assignment = assignmentRepository.save(assignment);

                    List<AssignmentSubmission> subs = new ArrayList<>();
                    for (StudentProfile sp : classStudents) {
                        double factor = performanceMap.getOrDefault(sp.getUser().getId(), 0.7);
                        SubmissionStatus status;
                        double r = rng.nextDouble();
                        if (r < factor * 0.9) {
                            status = SubmissionStatus.SUBMITTED;
                        } else if (r < factor * 0.95) {
                            status = SubmissionStatus.LATE;
                        } else {
                            status = SubmissionStatus.NOT_SUBMITTED;
                        }

                        subs.add(AssignmentSubmission.builder()
                                .assignment(assignment)
                                .student(sp.getUser())
                                .status(status)
                                .build());
                    }
                    assignmentSubmissionRepository.saveAll(subs);
                    aCount++;
                }
            }
        }
        log.info("[SampleDataSeeder] Seeded {} assignments with submissions.", aCount);
    }

    // ──────────────────────────────────────────────────────────────────────
    // LMS SCORES: one score per student per subject (0–100)
    // ──────────────────────────────────────────────────────────────────────
    private void seedLMSScoreData(List<StudentProfile> allStudents,
                                   Map<String, Subject> subjectMap,
                                   Map<String, ClassEntity> classMap,
                                   Map<String, User> teacherMap,
                                   Map<UUID, Double> performanceMap) {
        log.info("[SampleDataSeeder] Seeding LMS scores...");
        int count = 0;

        for (Map.Entry<String, ClassEntity> ce : classMap.entrySet()) {
            String className = ce.getKey();
            ClassEntity classEntity = ce.getValue();
            List<Subject> subjects = getSubjectsForClass(className, subjectMap);
            List<StudentProfile> classStudents = allStudents.stream()
                    .filter(sp -> sp.getClassEntity().getId().equals(classEntity.getId()))
                    .toList();

            for (Subject subject : subjects) {
                if (subject == null) continue;
                User teacher = findTeacher(subject.getCode(), className, teacherMap);
                if (teacher == null) teacher = teacherMap.values().iterator().next();

                List<LMSScore> scores = new ArrayList<>();
                for (StudentProfile sp : classStudents) {
                    double factor = performanceMap.getOrDefault(sp.getUser().getId(), 0.7);
                    double score = Math.round((factor * 100 + (rng.nextDouble() - 0.5) * 20) * 100.0) / 100.0;
                    score = Math.max(0, Math.min(100, score));

                    scores.add(LMSScore.builder()
                            .student(sp.getUser())
                            .subject(subject)
                            .classEntity(classEntity)
                            .teacher(teacher)
                            .score(score)
                            .build());
                    count++;
                }
                lmsScoreRepository.saveAll(scores);
            }
        }
        log.info("[SampleDataSeeder] Seeded {} LMS scores.", count);
    }
}
