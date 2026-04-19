package com.tarkshastra.app.dto.response;

import com.tarkshastra.app.enums.InterventionType;
import com.tarkshastra.app.enums.ActionItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterventionResponse {

    private UUID id;
    private String studentName;
    private UUID studentId;
    private String mentorName;
    private InterventionType interventionType;
    private LocalDate interventionDate;
    private String remarks;
    private LocalDate followUpDate;
    private Double preRiskScore;
    private Double postRiskScore;
    private Double scoreChange;
    private List<ActionItemResponse> actionItems;
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActionItemResponse {
        private UUID id;
        private String description;
        private ActionItemStatus status;
    }
}
