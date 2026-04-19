package com.tarkshastra.app.entity;

import com.tarkshastra.app.enums.RiskLabel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "risk_scores", indexes = {
        @Index(name = "idx_rs_student_latest", columnList = "student_id, is_latest"),
        @Index(name = "idx_rs_student_subject", columnList = "student_id, subject_id, is_latest"),
        @Index(name = "idx_rs_student_computed", columnList = "student_id, computed_at"),
        @Index(name = "idx_rs_institute_label", columnList = "risk_label")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskScore {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(name = "attendance_score", nullable = false)
    private Double attendanceScore;

    @Column(name = "marks_score", nullable = false)
    private Double marksScore;

    @Column(name = "assignment_score", nullable = false)
    private Double assignmentScore;

    @Column(name = "lms_score", nullable = false)
    private Double lmsScore;

    @Column(name = "risk_score", nullable = false)
    private Double riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_label", nullable = false, length = 10)
    private RiskLabel riskLabel;

    @Column(name = "is_latest", nullable = false)
    @Builder.Default
    private Boolean isLatest = true;

    @Column(name = "computed_at", nullable = false)
    private LocalDateTime computedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
