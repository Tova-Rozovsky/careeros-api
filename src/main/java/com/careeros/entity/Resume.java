package com.careeros.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "resumes")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Resume {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(nullable = false) private String fileName;
    @Column(nullable = false) private String storagePath;
    @Column(columnDefinition = "TEXT") private String parsedText;
    @Column(columnDefinition = "jsonb") private String parsedJson;
    private Integer atsScore;
    @Column(nullable = false) private Integer version;
    @Column(nullable = false) private Boolean isActive = true;
    @CreationTimestamp private LocalDateTime createdAt;
}