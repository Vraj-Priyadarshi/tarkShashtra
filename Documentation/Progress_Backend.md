# Backend Progress Tracker — Auth & Authorization Refactoring

## Status: COMPLETE ✅

All steps completed successfully. Project compiles cleanly with `mvn clean compile`.

**Last updated:** DataSeeder refactored to hardcoded 3-institute seed (no more application.properties seed keys).

---

## Summary of Changes

### Files Deleted
- `enums/AuthProvider.java`, `enums/Gender.java`
- `entity/VerificationToken.java`, `repository/VerificationTokenRepository.java`
- `controller/OAuth2Controller.java`, `service/OAuth2Service.java`
- `security/OAuth2AuthenticationSuccessHandler.java`, `security/OAuth2AuthenticationFailureHandler.java`
- `dto/request/SignupRequest.java`, `dto/request/SetPasswordRequest.java`, `dto/request/UpdateProfileRequest.java`

### Files Created
- `entity/Institute.java` — UUID-based institute with AISHE code
- `repository/InstituteRepository.java`
- `config/DataSeeder.java` — Hardcoded 3 institutes + coordinators (no properties needed):
  - **Pandit Deendayal Energy University** (U-0001) → `coordinator@pdeu.ac.in` / `PDEU@Coord2026`
  - **Nirma University** (U-0002) → `coordinator@nirmauni.ac.in` / `Nirma@Coord2026`
  - **LD Engineering University** (U-0003) → `coordinator@ldeuni.ac.in` / `LDE@Coord2026`
  - All coordinators have `mustChangePassword=true`; seeding is idempotent (skipped if AISHE code exists)

### Files Rewritten
- `enums/Role.java` — STUDENT, FACULTY_MENTOR, SUBJECT_TEACHER, ACADEMIC_COORDINATOR
- `entity/User.java` — UUID id, Set<Role>, ManyToOne Institute, mustChangePassword, isActive
- `dto/response/AuthResponse.java` — JWT + user info + mustChangePassword
- `dto/response/UserResponse.java` — Full user profile response
- `security/JwtService.java` — Claims: user_id, roles, institute_id, must_change_password
- `security/JwtAuthenticationFilter.java` — Blocks non-change-password endpoints when mustChangePassword=true
- `security/JwtAuthenticationEntryPoint.java` — Fixed Jackson imports
- `config/SecurityConfig.java` — Removed OAuth2, role-based access
- `service/AuthService.java` — Login, change-password, forgot/reset-password
- `service/UserService.java` — Simplified to getUserById, getCurrentUserProfile
- `service/EmailService.java` — MIME HTML emails, app name from properties
- `controller/AuthController.java` — Full auth endpoints
- `controller/UserController.java` — GET /me only

### Files Cleaned
- `util/Constants.java` — Removed verification constants, added PASSWORD_RESET_TOKEN_EXPIRATION_MINUTES
- `util/TokenGenerator.java` — Removed verification token, added generateTemporaryPassword
- `config/AppConfig.java` — Removed ModelMapper bean
- `repository/UserRepository.java` — Cleaned to findByEmail, existsByEmail
- `pom.xml` — Removed OAuth2, ModelMapper, AWS S3 dependencies
- `application.properties` — Removed OAuth2, added seed config, fixed logging
