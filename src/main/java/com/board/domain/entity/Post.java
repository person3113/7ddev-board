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
           @Index(name = "idx_deleted", columnList = "deleted"),
           @Index(name = "idx_notice", columnList = "notice"),
           // 검색 성능 최적화를 위한 복합 인덱스
           @Index(name = "idx_search_deleted_created", columnList = "deleted, createdAt"),
           @Index(name = "idx_search_category_deleted", columnList = "category, deleted, createdAt"),
           @Index(name = "idx_search_title_deleted", columnList = "title, deleted"),
           @Index(name = "idx_author_deleted_created", columnList = "author_id, deleted, createdAt")
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

    // 마크다운 지원을 위한 필드 추가
    @Column(nullable = false)
    private Boolean isMarkdown = false;

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

    // 공지사항 여부 (실무에서 필수)
    @Column(nullable = false, name = "notice")
    private Boolean isNotice = false;

    private LocalDateTime deletedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 연관관계 (양방향)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    @Builder
    public Post(String title, String content, String category, User author, Boolean isNotice, Boolean isMarkdown) {
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
        this.isNotice = isNotice != null ? isNotice : false;
        this.isMarkdown = isMarkdown != null ? isMarkdown : false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String title, String content, String category, Boolean isMarkdown) {
        validateTitle(title);
        validateContent(content);

        this.title = title;
        this.content = content;
        this.category = category;
        this.isMarkdown = isMarkdown != null ? isMarkdown : false;
        this.updatedAt = LocalDateTime.now();
    }

    // 공지사항 설정 (관리자만 가능)
    public void setNotice(boolean isNotice, User requestUser) {
        if (!requestUser.isAdmin()) {
            throw new IllegalArgumentException("공지사항은 관리자만 설정할 수 있습니다");
        }
        this.isNotice = isNotice;
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

    // 비즈니스 메서드
    public boolean canEdit(User user) {
        return isAuthor(user) || user.isAdmin();
    }

    public boolean canDelete(User user) {
        return isAuthor(user) || user.isAdmin();
    }

    // 공지사항 해제 가능 여부
    public boolean canToggleNotice(User user) {
        return user.isAdmin();
    }

    public int getCommentCount() {
        return this.comments.size();
    }

    public boolean isPopular() {
        return this.viewCount >= 100 || this.likeCount >= 10;
    }

    // 공지사항인지 확인
    public boolean isNotice() {
        return this.isNotice;
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
