package com.tarkshastra.app.service;

import com.tarkshastra.app.dto.request.ChangePasswordRequest;
import com.tarkshastra.app.dto.request.ForgotPasswordRequest;
import com.tarkshastra.app.dto.request.LoginRequest;
import com.tarkshastra.app.dto.request.PasswordResetRequest;
import com.tarkshastra.app.dto.response.AuthResponse;
import com.tarkshastra.app.dto.response.MessageResponse;
import com.tarkshastra.app.entity.PasswordResetToken;
import com.tarkshastra.app.entity.User;
import com.tarkshastra.app.exception.BadRequestException;
import com.tarkshastra.app.exception.ResourceNotFoundException;
import com.tarkshastra.app.repository.PasswordResetTokenRepository;
import com.tarkshastra.app.repository.UserRepository;
import com.tarkshastra.app.security.JwtService;
import com.tarkshastra.app.util.Constants;
import com.tarkshastra.app.util.TokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenGenerator tokenGenerator;
    private final EmailService emailService;

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new BadRequestException(Constants.INVALID_CREDENTIALS));

        String token = jwtService.generateToken(user);

        log.info("User logged in: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .roles(user.getRoles())
                .instituteId(user.getInstitute().getId())
                .mustChangePassword(user.isMustChangePassword())
                .build();
    }

    @Transactional
    public AuthResponse changePassword(User currentUser, ChangePasswordRequest request) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(Constants.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException(Constants.PASSWORD_MISMATCH);
        }

        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new BadRequestException(Constants.PASSWORD_CANNOT_BE_SAME_AS_OLD);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);

        // Issue a fresh JWT with must_change_password = false
        String token = jwtService.generateToken(user);

        log.info("Password changed for user: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .roles(user.getRoles())
                .instituteId(user.getInstitute().getId())
                .mustChangePassword(false)
                .build();
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException(Constants.USER_NOT_FOUND));

        String token = tokenGenerator.generatePasswordResetToken();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(Constants.PASSWORD_RESET_TOKEN_EXPIRATION_MINUTES))
                .build();

        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), token);

        log.info("Password reset requested for user: {}", user.getEmail());

        return MessageResponse.success(Constants.PASSWORD_RESET_EMAIL_SENT);
    }

    @Transactional
    public MessageResponse resetPassword(PasswordResetRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException(Constants.INVALID_TOKEN));

        if (resetToken.isExpired()) {
            throw new BadRequestException(Constants.TOKEN_EXPIRED);
        }

        if (resetToken.getIsUsed()) {
            throw new BadRequestException(Constants.TOKEN_ALREADY_USED);
        }

        User user = resetToken.getUser();

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException(Constants.PASSWORD_CANNOT_BE_SAME_AS_OLD);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);

        resetToken.setIsUsed(true);
        passwordResetTokenRepository.save(resetToken);

        log.info("Password reset successful for user: {}", user.getEmail());

        return MessageResponse.success(Constants.PASSWORD_RESET_SUCCESS);
    }

    public MessageResponse validateResetToken(String token) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException(Constants.INVALID_TOKEN));

        if (resetToken.isExpired()) {
            throw new BadRequestException(Constants.TOKEN_EXPIRED);
        }

        if (resetToken.getIsUsed()) {
            throw new BadRequestException(Constants.TOKEN_ALREADY_USED);
        }

        log.info("Password reset token validated successfully");

        return MessageResponse.success("Token is valid. You can now reset your password.");
    }
}