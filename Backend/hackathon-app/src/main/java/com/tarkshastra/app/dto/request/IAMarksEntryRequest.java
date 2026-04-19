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
public class IAMarksEntryRequest {

    @NotNull
    private UUID subjectId;

    @NotNull
    private UUID classId;

    @NotNull
    private String iaRound;

    @NotNull
    private Double maxMarks;

    @NotEmpty
    @Valid
    private List<IAMarkEntry> entries;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IAMarkEntry {
        @NotNull
        private UUID studentId;
        @NotNull
        private Double obtainedMarks;
        private boolean absent;
    }
}
