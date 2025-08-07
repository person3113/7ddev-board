package com.board.domain.entity;

import com.board.domain.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "reported_posts", indexes = {
    @Index(name = "idx_post_id", columnList = "post_id"),
    @Index(name = "idx_reporter_id", columnList = "reporter_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@ToString(exclude = {"post", "reporter"})
public class ReportedPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public ReportedPost(Post post, User reporter, String reason, ReportStatus status) {
        this.post = post;
        this.reporter = reporter;
        setReason(reason);
        this.status = status != null ? status : ReportStatus.PENDING;
    }

    public void setReason(String reason) {
        validateReason(reason);
        this.reason = reason;
    }

    public void validateReason(String reason) {
        if (reason != null && reason.length() > 500) {
            throw new IllegalArgumentException("신고 사유는 500자를 초과할 수 없습니다.");
        }
    }

    public void updateStatus(ReportStatus status) {
        this.status = status;
    }
}
