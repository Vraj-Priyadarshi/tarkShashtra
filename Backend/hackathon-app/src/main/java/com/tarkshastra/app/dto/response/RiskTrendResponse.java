package com.tarkshastra.app.dto.response;

import com.tarkshastra.app.enums.RiskLabel;
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
public class RiskTrendResponse {

    private UUID studentId;
    private List<RiskTrendPoint> dataPoints;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RiskTrendPoint {
        private LocalDate date;
        private Double riskScore;
        private RiskLabel riskLabel;
    }
}
