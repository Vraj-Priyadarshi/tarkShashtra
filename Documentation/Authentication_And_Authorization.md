# Authentication & Authorization — Entity Refactoring Instructions
### Project: Tarkshastra (TS-12 Early Academic Risk Detection Platform)
### Target Package Root: `com.tarkshastra.app`

---

## OVERVIEW

This document instructs Claude Code on exactly which files to **delete**, which to **create from scratch**, and which to **modify** for the authentication and authorization layer. No feature-level entities (risk scores, interventions, alerts, subjects) are included here — only what is strictly necessary for auth/authz to function correctly.

The system has **four roles**: `STUDENT`, `FACULTY_MENTOR`, `SUBJECT_TEACHER`, `ACADEMIC_COORDINATOR`.

A staff member can hold **multiple roles simultaneously** (e.g., both `FACULTY_MENTOR` and `SUBJECT_TEACHER`), which is why roles are stored as a **collection**, not a single column.

---

## STEP 1 — FILES TO DELETE ENTIRELY

Delete the following files. They are either from a previous project or support flows (OAuth, email verification, self-registration) that do not exist in this system.

```
com/tarkshastra/app/enums/AuthProvider.java      ← OAuth not used, all accounts are CSV-created
com/tarkshastra/app/enums/Gender.java            ← Profile field, not needed for auth
com/tarkshastra/app/entity/VerificationToken.java ← Email verification flow does not exist here
```

Also search the entire codebase for any imports or references to `AuthProvider`, `Gender`, and `VerificationToken` and remove them. If `isVerified` is referenced anywhere outside the User entity, remove those references too.

---

## STEP 2 — ENUMS

### 2.1 — DELETE and REPLACE: `com/tarkshastra/app/enums/Role.java`

The existing Role enum has wrong values (PLANNER, COMPANY, ANALYST, ADMIN). Replace the entire file contents with the following specification.

**File:** `com/tarkshastra/app/enums/Role.java`

```
Enum name    : Role
Package      : com.tarkshastra.app.enums
Values       : STUDENT, FACULTY_MENTOR, SUBJECT_TEACHER, ACADEMIC_COORDINATOR
Notes        : No additional fields or methods needed on the enum itself.
               A User can hold more than one of these values simultaneously
               (stored in a join table, not a single column on User).
```

---

## STEP 3 — NEW ENTITY: Institute

**File:** `com/tarkshastra/app/entity/Institute.java`  
**Table name:** `institutes`  
**Purpose:** Root entity. Every user (student, staff, coordinator) belongs to exactly one institute. This is the authorization boundary — all data queries are scoped by institute_id.

### Fields

| Field | Java Type | Column | Constraints | Notes |
|---|---|---|---|---|
| `id` | `UUID` | `id` | PK, not null, not updatable, auto UUID | Internal FK target used across all entities |
| `aisheCode` | `String` | `aishe_code` | unique, not null, length = 20 | Real-world unique identifier assigned by Govt. of India. Example format: `C-45612`. No two institutes share this. |
| `name` | `String` | `name` | not null, length = 200 | Full official institute name |
| `createdAt` | `LocalDateTime` | `created_at` | not null, not updatable, auto-populated | Use `@CreationTimestamp` |

### Indexes
- Index on `aishe_code` column. Name: `idx_institute_aishe_code`

### JPA Annotations
- `@Entity`
- `@Table(name = "institutes")`
- Lombok: `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`

### Relationships (for auth scope only)
No relationship fields needed on the Institute entity itself at this stage. The FK lives on the User side.

---

## STEP 4 — REFACTOR ENTITY: User

**File:** `com/tarkshastra/app/entity/User.java`  
**Table name:** `users`  
**Purpose:** Unified identity entity for all user types. Implements Spring Security's `UserDetails`. All logins (student, staff, coordinator) go through this entity using email + password.

### Fields — KEEP

| Field | Java Type | Column | Constraints | Notes |
|---|---|---|---|---|
| `id` | `UUID` | `id` | PK, not null, not updatable, auto UUID | No change |
| `email` | `String` | `email` | unique, not null, length = 120, `@Email` `@NotBlank` | Username for Spring Security. All user types log in with email. |
| `passwordHash` | `String` | `password_hash` | not null, length = 255, `@JsonIgnore` | BCrypt hash. System sets a temporary value on CSV creation; user replaces it on first login. |
| `createdAt` | `LocalDateTime` | `created_at` | not null, not updatable | `@CreationTimestamp` |

### Fields — REMOVE from existing User entity

Remove these fields and all associated annotations, imports, and column definitions:

```
firstName        ← profile field, not auth
lastName         ← profile field, not auth
dob              ← profile field, not auth
phone            ← profile field, not auth
gender           ← profile field, not auth (and Gender enum is deleted)
authProvider     ← OAuth removed (AuthProvider enum is deleted)
googleId         ← OAuth removed
isVerified       ← replaced by isActive (see below)
role (single)    ← replaced by roles collection (see below)
```

### Fields — ADD (new fields not in existing entity)

| Field | Java Type | Column | Constraints | Notes |
|---|---|---|---|---|
| `roles` | `Set<Role>` | stored in join table `user_roles` | not null, fetch = EAGER, min size 1 | See join table specification below. EAGER is required — Spring Security calls `getAuthorities()` on every request and lazy loading causes `LazyInitializationException` outside a session. |
| `institute` | `Institute` | FK column `institute_id` on `users` table | not null, `FetchType.LAZY`, `@ManyToOne` | Every user belongs to exactly one institute. This is the authorization scope. |
| `mustChangePassword` | `boolean` (primitive) | `must_change_password` | not null, default = `true` | Set to `true` when account is created via CSV upload. Flipped to `false` after user successfully sets their own password. Included as a custom claim in the issued JWT. Does NOT block login — it restricts post-login access. |
| `isActive` | `Boolean` (wrapper) | `is_active` | not null, default = `true` | Controls whether the account functions at all. Used in `isEnabled()` of UserDetails. Set to `false` to suspend/deactivate an account without deleting it. |

### Join Table Specification: `user_roles`

Stores the many roles a single user holds. Managed automatically by JPA via `@ElementCollection`.

| Column | Type | Constraints |
|---|---|---|
| `user_id` | UUID | FK to `users.id`, not null |
| `role` | VARCHAR(30) | not null, stores enum name as string |

JPA annotation to use on the `roles` field in User:
```
@ElementCollection(fetch = FetchType.EAGER)
@CollectionTable(
    name = "user_roles",
    joinColumns = @JoinColumn(name = "user_id")
)
@Enumerated(EnumType.STRING)
@Column(name = "role", length = 30, nullable = false)
```
Initialize the field as: `private Set<Role> roles = new HashSet<>();`  
With `@Builder`, use `@Builder.Default` on this field.

### Indexes on `users` table

```
idx_user_email         → columnList = "email"
idx_user_institute_id  → columnList = "institute_id"   ← NEW, needed for scoped queries
```

Remove the existing `idx_user_google_id` index — that column no longer exists.

### UserDetails Implementation — Updated Method Bodies

Update these method implementations in User. Keep all `@JsonIgnore` and `@Override` annotations.

**`getAuthorities()`**
```
Maps every Role in the roles Set to a SimpleGrantedAuthority with prefix "ROLE_".
Returns a Set<GrantedAuthority> — one entry per role in the user's roles set.
Example: a user with {FACULTY_MENTOR, SUBJECT_TEACHER} returns
  [ROLE_FACULTY_MENTOR, ROLE_SUBJECT_TEACHER]
Do NOT return a singleton collection — iterate the full Set.
```

**`getUsername()`**
```
Returns: this.email
No change from existing implementation.
```

**`getPassword()`**
```
Returns: this.passwordHash
No change from existing implementation.
```

**`isEnabled()`**
```
Returns: this.isActive
Previously returned isVerified — replace with isActive.
```

**`isAccountNonExpired()`**
```
Returns: true
No change.
```

**`isAccountNonLocked()`**
```
Returns: true
No change.
```

**`isCredentialsNonExpired()`**
```
Returns: true
No change.
```

### Imports to add to User.java

```java
import com.tarkshastra.app.entity.Institute;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
```

### Imports to REMOVE from User.java

```java
import com.tarkshastra.app.enums.AuthProvider;
import com.tarkshastra.app.enums.Gender;
// Any import for Collections.singleton if it was used in getAuthorities()
```

---

## STEP 5 — KEEP AND MINOR UPDATE: PasswordResetToken

**File:** `com/tarkshastra/app/entity/PasswordResetToken.java`  
**Table name:** `password_reset_tokens`

The existing entity structure is correct. The only reason to touch this file is that it references `User`, and `User` is being refactored. Since the class name and package of `User` remain the same, **no import changes are needed**.

### Verify the following are still correct (do not change):

- `@ManyToOne(fetch = FetchType.LAZY)` on the `user` field — correct. One user can have multiple reset tokens (only the latest valid one matters in logic, but the DB allows multiples).
- `isExpired()` helper method — keep as-is.
- `isUsed` field with `@Builder.Default` value of `false` — keep as-is.
- UUID primary key — keep as-is.
- Index on `token` column — keep as-is.

### No changes needed to this entity.

---

## STEP 6 — COORDINATOR ACCOUNT SEEDING

The Academic Coordinator account is **not created via CSV**. It is the root administrator for an institute and must exist before any CSVs can be uploaded. Use a Spring `ApplicationRunner` or `CommandLineRunner` bean (in a `DataLoader` or `DatabaseSeeder` component) to seed the initial institute and coordinator account on application startup if they do not already exist.

**This seeder should be placed in:** `com/tarkshastra/app/config/DataSeeder.java` or `com/tarkshastra/app/runner/DataSeeder.java`

### Seeder behaviour specification

1. Read institute name, AISHE code, coordinator email, and coordinator temporary password from `application.yml` (under a custom namespace, e.g., `app.seed.*`).
2. Check if an Institute with that AISHE code already exists in the DB. If yes, skip entirely.
3. If not, create and save the `Institute` record.
4. Create a `User` record with:
   - The email from config
   - Password: BCrypt hash of the temporary password from config
   - `roles` = `{ACADEMIC_COORDINATOR}`
   - `institute` = the newly created Institute
   - `mustChangePassword` = `true`
   - `isActive` = `true`
5. Save the User record.
6. Log a startup message: `"[DataSeeder] Institute and coordinator account seeded successfully."`

### application.yml properties to add

```yaml
app:
  seed:
    institute-name: "Demo Engineering College"
    aishe-code: "C-99999"
    coordinator-email: "coordinator@demo.edu"
    coordinator-temp-password: "Admin@1234"
```

---

## STEP 7 — JWT CLAIMS SPECIFICATION

When issuing a JWT after successful login, the token payload must include the following claims. This is for the JWT utility/service class — not an entity, but documenting here for completeness of the auth layer.

| Claim Key | Value | Notes |
|---|---|---|
| `sub` | user's email | Standard JWT subject claim |
| `user_id` | user's UUID as String | For backend lookups without DB hit |
| `roles` | List of role name strings | e.g., `["FACULTY_MENTOR","SUBJECT_TEACHER"]` |
| `institute_id` | institute UUID as String | Used for data scoping in all queries |
| `must_change_password` | boolean | Frontend reads this — if `true`, redirect to forced password change screen before rendering any dashboard |
| `iat` | issued-at timestamp | Standard |
| `exp` | expiry timestamp | Standard |

---

## STEP 8 — ENTITY RELATIONSHIP SUMMARY (Auth scope only)

```
Institute  (1) ──────────────── (many)  User
                                         |
                                    Set<Role>  →  user_roles join table
                                         |
User       (1) ──────────────── (many)  PasswordResetToken
```

- One `Institute` → many `User` records (students, staff, coordinator all linked to same institute)
- One `User` → many `Role` values (via `user_roles` join table, `@ElementCollection`)
- One `User` → many `PasswordResetToken` records (`@ManyToOne` from token side)

---

## STEP 9 — FINAL CHECKLIST FOR CLAUDE CODE

Work through this list in order:

- [ ] Delete `AuthProvider.java`
- [ ] Delete `Gender.java`
- [ ] Delete `VerificationToken.java`
- [ ] Replace all enum values in `Role.java` with: `STUDENT, FACULTY_MENTOR, SUBJECT_TEACHER, ACADEMIC_COORDINATOR`
- [ ] Create `Institute.java` entity with fields from Step 3
- [ ] Refactor `User.java` — remove dead fields, add new fields, update all UserDetails methods, fix indexes
- [ ] Verify `PasswordResetToken.java` compiles cleanly with the refactored `User` — no changes expected
- [ ] Scan entire codebase for references to removed fields/enums and clean them up: `isVerified`, `AuthProvider`, `Gender`, `googleId`, `authProvider`, `role` (singular on User), `firstName`, `lastName`, `dob`
- [ ] Add JWT claims as specified in Step 7 to the JWT utility/service class
- [ ] Create `DataSeeder` component for coordinator seeding with `application.yml` config
- [ ] Confirm `user_roles` join table is correctly specified via `@ElementCollection` with `FetchType.EAGER`
- [ ] Confirm `institute_id` FK and index exist on the `users` table
- [ ] Run `mvn clean compile` and resolve any remaining import or type errors

---

## IMPORTANT NOTES FOR CLAUDE CODE

1. The existing `PasswordResetToken` uses `@ManyToOne` to `User`. After refactoring `User`, this still compiles correctly because the class name and package do not change.

2. The `mustChangePassword` field is a **primitive `boolean`** (not `Boolean` wrapper) to avoid null issues — it always has a value. The `isActive` field is a **`Boolean` wrapper** intentionally, but should default to `true` via `@Builder.Default`.

3. Do **not** add `@NotNull` Bean Validation on the `roles` Set — validation of a collection's emptiness is done at the service layer before saving, not via Bean Validation on the entity.

4. The `institute` field on `User` uses `FetchType.LAZY` — we do not need the full Institute object loaded on every authentication check. `institute_id` is embedded as a JWT claim so the service layer can scope queries without a DB join on every request.

5. Do **not** remove `@ToString(exclude = {"passwordHash"})` from the User entity if it exists — this prevents accidental password hash logging.
