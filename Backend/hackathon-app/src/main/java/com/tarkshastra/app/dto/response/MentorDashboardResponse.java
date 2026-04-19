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
public class MentorDashboardResponse {

    private int totalMentees;
    private int highRiskMentees;
    private int mediumRiskMentees;
    private int lowRiskMentees;
    private int unresolvedFlags;
    private int pendingFollowUps;
    private List<StudentProfileResponse> menteeSummary;
}
