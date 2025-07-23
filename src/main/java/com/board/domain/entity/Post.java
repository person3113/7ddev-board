package com.board.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts",
       indexes = {
           @Index(name = "idx_category", columnList = "category"),
           @Index(name = "idx_created_at", columnList = "createdAt"),
           @Index(name = "idx_deleted", columnList = "deleted")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@ToString(exclude = {"author", "comments"})
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 50)
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false)
    private Integer viewCount = 0;

    @Column(nullable = false)
    private Integer likeCount = 0;

    @Column(nullable = false)
    private Boolean deleted = false;

    private LocalDateTime deletedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 연관관계 추가 (양방향)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    @Builder
    public Post(String title, String content, String category, User author) {
        validateTitle(title);
        validateContent(content);
        validateAuthor(author);

        this.title = title;
        this.content = content;
        this.category = category;
        this.author = author;
        this.viewCount = 0;
        this.likeCount = 0;
        this.deleted = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String title, String content, String category) {
        validateTitle(title);
        validateContent(content);

        this.title = title;
        this.content = content;
        this.category = category;
        this.updatedAt = LocalDateTime.now();
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void delete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isAuthor(User user) {
        return this.author.equals(user);
    }

    public boolean isDeleted() {
        return this.deleted;
    }

    // 비즈니스 메서드 추가
    public boolean canEdit(User user) {
        return isAuthor(user) || user.isAdmin();
    }

    public boolean canDelete(User user) {
        return isAuthor(user) || user.isAdmin();
    }

    public int getCommentCount() {
        return this.comments.size();
    }

    public boolean isPopular() {
        return this.viewCount >= 100 || this.likeCount >= 10;
    }

    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 필수입니다");
        }
        if (title.length() > 200) {
            throw new IllegalArgumentException("제목은 200자를 초과할 수 없습니다");
        }
    }

    private void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("내용은 필수입니다");
        }
    }

    private void validateAuthor(User author) {
        if (author == null) {
            throw new IllegalArgumentException("작성자는 필수입니다");
        }
    }
}
