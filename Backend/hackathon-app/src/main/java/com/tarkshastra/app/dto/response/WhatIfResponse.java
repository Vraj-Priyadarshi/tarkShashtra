package com.tarkshastra.app.dto.response;

import com.tarkshastra.app.enums.RiskLabel;
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
public class WhatIfResponse {

    private Double currentRiskScore;
    private RiskLabel currentRiskLabel;
    private Double predictedRiskScore;
    private RiskLabel predictedRiskLabel;
    private List<RiskScoreResponse.SubjectRiskResponse> subjectPredictions;
}
