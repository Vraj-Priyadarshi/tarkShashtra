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
public class LMSScoreBulkRequest {

    @NotNull
    private UUID subjectId;

    @NotNull
    private UUID classId;

    @NotEmpty
    @Valid
    private List<LMSScoreEntry> entries;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LMSScoreEntry {
        @NotNull
        private UUID studentId;
        @NotNull
        private Double score;
    }
}
