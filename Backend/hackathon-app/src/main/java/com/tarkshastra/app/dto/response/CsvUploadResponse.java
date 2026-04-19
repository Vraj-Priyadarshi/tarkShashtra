package com.tarkshastra.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CsvUploadResponse {

    private int totalRows;
    private int successCount;
    private int errorCount;
    private List<CsvRowError> errors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CsvRowError {
        private int rowNumber;
        private String field;
        private String message;
    }
}
