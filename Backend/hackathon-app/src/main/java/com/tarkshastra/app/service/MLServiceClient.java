package com.tarkshastra.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MLServiceClient {

    private final WebClient mlWebClient;

    @Value("${app.ml-service.timeout:5000}")
    private int timeout;

    /**
     * Calls the ML /api/predict endpoint with feature scores.
     * Returns the predicted risk score (0-100).
     * Falls back to weighted average if ML service is unavailable.
     */
    public Double predictRiskScore(Double attendance, Double marks, Double assignment, Double lms) {
        try {
            Map<String, Object> payload = Map.of(
                    "attendance", attendance != null ? attendance : 0.0,
                    "marks", marks != null ? marks : 0.0,
                    "assignment", assignment != null ? assignment : 0.0,
                    "lms", lms != null ? lms : 0.0
            );

            Map<String, Object> response = mlWebClient.post()
                    .uri("/api/predict")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();

            if (response != null && response.containsKey("risk_score")) {
                Object score = response.get("risk_score");
                return score instanceof Number ? ((Number) score).doubleValue() : null;
            }
        } catch (Exception e) {
            log.warn("ML service unavailable, falling back to weighted average: {}", e.getMessage());
        }

        // Fallback: weighted average
        return computeFallbackScore(attendance, marks, assignment, lms);
    }

    private Double computeFallbackScore(Double attendance, Double marks, Double assignment, Double lms) {
        double att = attendance != null ? attendance : 50.0;
        double mk = marks != null ? marks : 50.0;
        double asg = assignment != null ? assignment : 50.0;
        double lm = lms != null ? lms : 50.0;

        // Risk = 100 - weighted performance
        double performance = (att * 0.30) + (mk * 0.30) + (asg * 0.25) + (lm * 0.15);
        return Math.max(0, Math.min(100, 100 - performance));
    }

    /**
     * Calls the ML /api/suggestions endpoint with full student data.
     * Returns raw JSON response as a Map.
     */
    public Map<String, Object> getSuggestions(Map<String, Object> studentPayload) {
        try {
            Map<String, Object> response = mlWebClient.post()
                    .uri("/api/suggestions")
                    .bodyValue(studentPayload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofMillis(30000))
                    .block();
            return response;
        } catch (Exception e) {
            log.warn("ML suggestions endpoint unavailable: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Calls the ML /api/roadmap endpoint with full student data.
     * Returns raw JSON response as a Map.
     */
    public Map<String, Object> getRoadmap(Map<String, Object> studentPayload) {
        try {
            Map<String, Object> response = mlWebClient.post()
                    .uri("/api/roadmap")
                    .bodyValue(studentPayload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofMillis(30000))
                    .block();
            return response;
        } catch (Exception e) {
            log.warn("ML roadmap endpoint unavailable: {}", e.getMessage());
            return null;
        }
    }
}
