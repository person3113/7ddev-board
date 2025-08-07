package com.board.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_likes",
       indexes = {
           @Index(name = "idx_post_user", columnList = "post_id, user_id"),
           @Index(name = "idx_user_created", columnList = "user_id, createdAt"),
           @Index(name = "idx_post_created", columnList = "post_id, createdAt")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_post_user", columnNames = {"post_id", "user_id"})
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@ToString(exclude = {"post", "user"})
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Boolean isLike; // true: 추천, false: 비추천

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public PostLike(Post post, User user, Boolean isLike) {
        validatePost(post);
        validateUser(user);
        validateIsLike(isLike);

        this.post = post;
        this.user = user;
        this.isLike = isLike;
    }

    public void updateLike(Boolean isLike) {
        validateIsLike(isLike);
        this.isLike = isLike;
    }

    private void validatePost(Post post) {
        if (post == null) {
            throw new IllegalArgumentException("게시글은 필수입니다");
        }
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("사용자는 필수입니다");
        }
    }

    private void validateIsLike(Boolean isLike) {
        if (isLike == null) {
            throw new IllegalArgumentException("추천/비추천 여부는 필수입니다");
        }
    }
}
