package com.tarkshastra.app.dto.request;

import com.tarkshastra.app.enums.RiskLabel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentFilterRequest {

    private UUID departmentId;
    private UUID classId;
    private RiskLabel riskLabel;
    private String search;
    private int page = 0;
    private int size = 20;
    private String sortBy = "fullName";
    private String sortDir = "asc";
}
