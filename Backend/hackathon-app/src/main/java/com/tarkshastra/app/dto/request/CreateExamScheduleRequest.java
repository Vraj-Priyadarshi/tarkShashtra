package com.tarkshastra.app.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateExamScheduleRequest {

    @NotNull
    private UUID subjectId;

    @NotNull
    private UUID classId;

    @NotNull
    private LocalDate examDate;

    @NotNull
    private String examType;
}
