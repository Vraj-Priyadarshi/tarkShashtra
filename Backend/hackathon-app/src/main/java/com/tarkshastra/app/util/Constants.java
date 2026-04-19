package com.tarkshastra.app.util;

public class Constants {

    // Token Expiration
    public static final int PASSWORD_RESET_TOKEN_EXPIRATION_MINUTES = 15;

    // Email Subjects
    public static final String PASSWORD_RESET_EMAIL_SUBJECT = "Reset Your Password";

    // Validation Messages
    public static final String INVALID_TOKEN = "Invalid or expired token";
    public static final String TOKEN_EXPIRED = "Token has expired";
    public static final String TOKEN_ALREADY_USED = "Token has already been used";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String INVALID_CREDENTIALS = "Invalid email or password";
    public static final String PASSWORD_MISMATCH = "Current password is incorrect";
    public static final String PASSWORD_CANNOT_BE_SAME_AS_OLD = "New password cannot be the same as the old password";

    // Success Messages
    public static final String PASSWORD_RESET_EMAIL_SENT = "Password reset instructions have been sent to your email.";
    public static final String PASSWORD_RESET_SUCCESS = "Password has been reset successfully.";
    public static final String PASSWORD_CHANGE_SUCCESS = "Password changed successfully.";

    private Constants() {
    }
}