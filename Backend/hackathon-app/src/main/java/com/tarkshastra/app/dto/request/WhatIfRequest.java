package com.tarkshastra.app.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhatIfRequest {

    @NotNull
    private UUID studentId;

    @NotEmpty
    @Valid
    private List<WhatIfSubjectEntry> hypotheticalSubjects;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WhatIfSubjectEntry {
        @NotNull
        private UUID subjectId;
        @NotNull
        private Double attendance;
        @NotNull
        private Double marks;
        @NotNull
        private Double assignment;
        @NotNull
        private Double lms;
    }
}
