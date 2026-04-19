package com.tarkshastra.app.service;

import com.tarkshastra.app.util.Constants;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from:noreply@tarkshastra.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${spring.application.name:TarkShastra}")
    private String appName;

    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + token;
            String subject = Constants.PASSWORD_RESET_EMAIL_SUBJECT;
            String html = buildPasswordResetEmailHtml(resetUrl);
            sendHtmlEmail(toEmail, subject, html);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
        }
    }

    public void sendTemporaryPasswordEmail(String toEmail, String tempPassword) {
        try {
            String subject = "Your Account Has Been Created — " + appName;
            String html = buildTemporaryPasswordEmailHtml(toEmail, tempPassword);
            sendHtmlEmail(toEmail, subject, html);
            log.info("Temporary password email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send temporary password email to: {}", toEmail, e);
        }
    }

    public void sendAlertEmail(String toEmail, String title, String messageBody) {
        try {
            String html = """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head><meta charset="UTF-8"></head>
                    <body style="font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 0;">
                      <div style="max-width: 520px; margin: 40px auto; background: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.07); overflow: hidden;">
                        <div style="background: #d93025; padding: 24px 32px;">
                          <h1 style="color: #ffffff; margin: 0; font-size: 22px;">%s</h1>
                        </div>
                        <div style="padding: 32px;">
                          <p style="color: #333; font-size: 15px; line-height: 1.6;">%s</p>
                          <hr style="border: none; border-top: 1px solid #eee; margin: 24px 0;">
                          <p style="color: #999; font-size: 12px; text-align: center;">%s &mdash; Early Academic Risk Detection Platform</p>
                        </div>
                      </div>
                    </body>
                    </html>
                    """.formatted(title, messageBody, appName);
            sendHtmlEmail(toEmail, title + " — " + appName, html);
            log.info("Alert email sent to: {} ({})", toEmail, title);
        } catch (Exception e) {
            log.error("Failed to send alert email to: {}", toEmail, e);
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(message);
    }

    private String buildPasswordResetEmailHtml(String resetUrl) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head><meta charset="UTF-8"></head>
                <body style="font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 0;">
                  <div style="max-width: 520px; margin: 40px auto; background: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.07); overflow: hidden;">
                    <div style="background: #1a73e8; padding: 24px 32px;">
                      <h1 style="color: #ffffff; margin: 0; font-size: 22px;">%s</h1>
                    </div>
                    <div style="padding: 32px;">
                      <p style="color: #333; font-size: 15px; line-height: 1.6;">Hello,</p>
                      <p style="color: #333; font-size: 15px; line-height: 1.6;">We received a request to reset your password. Click the button below to set a new password:</p>
                      <div style="text-align: center; margin: 28px 0;">
                        <a href="%s" style="background-color: #1a73e8; color: #ffffff; padding: 12px 32px; text-decoration: none; border-radius: 6px; font-size: 15px; font-weight: 600; display: inline-block;">Reset Password</a>
                      </div>
                      <p style="color: #666; font-size: 13px; line-height: 1.6;">This link will expire in %d minutes. If you didn't request this, please ignore this email.</p>
                      <hr style="border: none; border-top: 1px solid #eee; margin: 24px 0;">
                      <p style="color: #999; font-size: 12px; text-align: center;">%s &mdash; Early Academic Risk Detection Platform</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(appName, resetUrl, Constants.PASSWORD_RESET_TOKEN_EXPIRATION_MINUTES, appName);
    }

    private String buildTemporaryPasswordEmailHtml(String email, String tempPassword) {
        String loginUrl = frontendUrl + "/login";
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head><meta charset="UTF-8"></head>
                <body style="font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 0;">
                  <div style="max-width: 520px; margin: 40px auto; background: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.07); overflow: hidden;">
                    <div style="background: #1a73e8; padding: 24px 32px;">
                      <h1 style="color: #ffffff; margin: 0; font-size: 22px;">Welcome to %s</h1>
                    </div>
                    <div style="padding: 32px;">
                      <p style="color: #333; font-size: 15px; line-height: 1.6;">Hello,</p>
                      <p style="color: #333; font-size: 15px; line-height: 1.6;">An account has been created for you on <strong>%s</strong>. Use the credentials below to log in:</p>
                      <div style="background: #f8f9fa; border-radius: 6px; padding: 16px 20px; margin: 20px 0;">
                        <p style="margin: 4px 0; font-size: 14px;"><strong>Email:</strong> %s</p>
                        <p style="margin: 4px 0; font-size: 14px;"><strong>Temporary Password:</strong> <code style="background: #e8eaed; padding: 2px 8px; border-radius: 4px;">%s</code></p>
                      </div>
                      <p style="color: #d93025; font-size: 14px; font-weight: 600;">You will be required to change your password on first login.</p>
                      <div style="text-align: center; margin: 28px 0;">
                        <a href="%s" style="background-color: #1a73e8; color: #ffffff; padding: 12px 32px; text-decoration: none; border-radius: 6px; font-size: 15px; font-weight: 600; display: inline-block;">Log In Now</a>
                      </div>
                      <hr style="border: none; border-top: 1px solid #eee; margin: 24px 0;">
                      <p style="color: #999; font-size: 12px; text-align: center;">%s &mdash; Early Academic Risk Detection Platform</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(appName, appName, email, tempPassword, loginUrl, appName);
    }
}