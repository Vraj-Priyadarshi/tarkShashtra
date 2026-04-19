package com.tarkshastra.app.dto.response;

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
public class StudentFlagResponse {

    private UUID id;
    private String studentName;
    private UUID studentId;
    private String flaggedByName;
    private String subjectName;
    private String note;
    private boolean resolved;
    private LocalDateTime createdAt;
}
