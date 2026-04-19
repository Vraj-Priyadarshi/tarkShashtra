package com.tarkshastra.app.service;

import com.tarkshastra.app.dto.response.UserResponse;
import com.tarkshastra.app.entity.StudentProfile;
import com.tarkshastra.app.entity.TeacherProfile;
import com.tarkshastra.app.entity.User;
import com.tarkshastra.app.exception.ResourceNotFoundException;
import com.tarkshastra.app.repository.StudentProfileRepository;
import com.tarkshastra.app.repository.TeacherProfileRepository;
import com.tarkshastra.app.repository.UserRepository;
import com.tarkshastra.app.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.USER_NOT_FOUND));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.USER_NOT_FOUND));
    }

    public UserResponse getCurrentUserProfile(UUID userId) {
        User user = getUserById(userId);
        return mapToUserResponse(user);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(user.getRoles())
                .instituteId(user.getInstitute().getId())
                .instituteName(user.getInstitute().getName())
                .mustChangePassword(user.isMustChangePassword())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt());

        // Enrich with student profile data
        studentProfileRepository.findByUserId(user.getId()).ifPresent(sp -> {
            builder.rollNumber(sp.getRollNumber())
                    .departmentName(sp.getDepartment().getName())
                    .className(sp.getClassEntity().getName())
                    .semester(sp.getSemester())
                    .mentorName(sp.getMentor() != null ? sp.getMentor().getFullName() : null);
        });

        // Enrich with teacher profile data
        teacherProfileRepository.findByUserId(user.getId()).ifPresent(tp -> {
            builder.employeeId(tp.getEmployeeId())
                    .departmentName(tp.getDepartment().getName());
        });

        return builder.build();
    }
}