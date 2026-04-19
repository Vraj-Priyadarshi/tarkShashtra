package com.tarkshastra.app.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlagStudentRequest {

    @NotNull
    private UUID studentId;

    @NotNull
    private UUID subjectId;

    private String note;
}
