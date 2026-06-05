package com.careeros.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "job_applications")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class JobApplication {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(nullable = false) private String companyName;
    @Column(nullable = false) private String roleTitle;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ApplicationStatus status = ApplicationStatus.WISHLIST;
    private String jdUrl;
    @Column(columnDefinition = "TEXT") private String jdText;
    private LocalDate appliedDate;
    private String notes;
    private LocalDate followUpDate;
    private Integer atsScoreAtApply;
    @CreationTimestamp private LocalDateTime createdAt;
    public enum ApplicationStatus { WISHLIST, APPLIED, SCREENING, INTERVIEW, OFFER, REJECTED }
}