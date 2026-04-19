package com.tarkshastra.app.dto.request;

import com.tarkshastra.app.enums.SubmissionStatus;
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
public class MarkSubmissionRequest {

    @NotNull
    private UUID assignmentId;

    @NotEmpty
    @Valid
    private List<SubmissionEntry> submissions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubmissionEntry {
        @NotNull
        private UUID studentId;
        @NotNull
        private SubmissionStatus status;
    }
}
