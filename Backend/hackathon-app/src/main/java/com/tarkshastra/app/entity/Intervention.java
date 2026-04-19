package com.tarkshastra.app.entity;

import com.tarkshastra.app.enums.InterventionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "interventions", indexes = {
        @Index(name = "idx_int_student", columnList = "student_id"),
        @Index(name = "idx_int_mentor", columnList = "mentor_id"),
        @Index(name = "idx_int_followup", columnList = "follow_up_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Intervention {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    @Enumerated(EnumType.STRING)
    @Column(name = "intervention_type", nullable = false, length = 30)
    private InterventionType interventionType;

    @Column(name = "intervention_date", nullable = false)
    private LocalDate interventionDate;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    @Column(name = "pre_risk_score")
    private Double preRiskScore;

    @Column(name = "post_risk_score")
    private Double postRiskScore;

    @OneToMany(mappedBy = "intervention", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActionItem> actionItems;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
