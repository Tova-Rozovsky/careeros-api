package com.careeros.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "users")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false, unique = true) private String email;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private String passwordHash;
    private String targetRole;
    // @Column(columnDefinition = "jsonb") private String targetCompanies;
    @Column(columnDefinition = "jsonb")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON) private String targetCompanies;
    @Enumerated(EnumType.STRING) private ExperienceLevel experienceLevel;
    @CreationTimestamp private LocalDateTime createdAt;
    @UpdateTimestamp private LocalDateTime updatedAt;
    public enum ExperienceLevel { FRESHER, JUNIOR, MID, SENIOR }
}