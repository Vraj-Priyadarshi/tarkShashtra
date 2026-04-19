package com.tarkshastra.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsistencyStreakResponse {

    private int currentStreak;
    private int longestStreak;
    private LocalDate lastQualifyingWeek;
}
