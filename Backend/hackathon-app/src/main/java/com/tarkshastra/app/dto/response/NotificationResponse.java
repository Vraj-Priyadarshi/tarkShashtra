package com.tarkshastra.app.dto.response;

import com.tarkshastra.app.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private UUID id;
    private String title;
    private String message;
    private NotificationType notificationType;
    private boolean read;
    private UUID referenceId;
    private String referenceType;
    private LocalDateTime createdAt;
}
