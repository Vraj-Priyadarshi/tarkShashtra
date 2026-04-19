package com.tarkshastra.app.dto.request;

import com.tarkshastra.app.enums.InterventionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateInterventionRequest {

    @NotNull
    private UUID studentId;

    @NotNull
    private InterventionType interventionType;

    @NotNull
    private LocalDate interventionDate;

    private String remarks;

    private LocalDate followUpDate;

    private List<String> actionItems;
}
