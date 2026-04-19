package com.tarkshastra.app.dto.request;

import com.tarkshastra.app.enums.AttendanceEntryMode;
import com.tarkshastra.app.enums.AttendanceStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
public class AttendanceSessionRequest {

    @NotNull
    private UUID subjectId;

    @NotNull
    private UUID classId;

    @NotNull
    private LocalDate sessionDate;

    @NotNull
    private AttendanceEntryMode entryMode;

    @NotEmpty
    @Valid
    private List<AttendanceRecordRequest> records;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AttendanceRecordRequest {
        @NotNull
        private UUID studentId;
        private AttendanceStatus status;
        private Double bulkPercentage;
    }
}
