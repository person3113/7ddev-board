package com.board.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment_likes",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_comment_like_user", columnNames = {"comment_id", "user_id"})
       },
       indexes = {
           @Index(name = "idx_comment_id", columnList = "comment_id"),
           @Index(name = "idx_user_id", columnList = "user_id"),
           @Index(name = "idx_created_at", columnList = "createdAt")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@ToString(exclude = {"comment", "user"})
public class CommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public CommentLike(Comment comment, User user) {
        validateComment(comment);
        validateUser(user);

        this.comment = comment;
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }

    private void validateComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("댓글은 필수입니다");
        }
        if (comment.isDeleted()) {
            throw new IllegalArgumentException("삭제된 댓글에는 좋아요를 누를 수 없습니다");
        }
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("사용자는 필수입니다");
        }
    }

    // 비즈니스 메서드
    public boolean isLikedBy(User user) {
        return this.user.equals(user);
    }
}
