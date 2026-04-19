package com.tarkshastra.app.dto.response;

import com.tarkshastra.app.enums.InterventionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterventionEffectivenessResponse {

    private InterventionType interventionType;
    private long count;
    private Double avgPreScore;
    private Double avgPostScore;
    private Double avgImprovement;
}
