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
@Table(name = "comments",
       indexes = {
           @Index(name = "idx_post_id", columnList = "post_id"),
           @Index(name = "idx_created_at", columnList = "createdAt"),
           @Index(name = "idx_deleted", columnList = "deleted"),
           @Index(name = "idx_parent_id", columnList = "parent_id")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@ToString(exclude = {"post", "author", "parent", "children"})
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<Comment> children = new ArrayList<>();

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

    @Builder
    public Comment(String content, Post post, User author, Comment parent) {
        validateContent(content);
        validatePost(post);
        validateAuthor(author);
        validateParentDepth(parent);

        this.content = content;
        this.post = post;
        this.author = author;
        this.parent = parent;
        this.likeCount = 0;
        this.deleted = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // 부모 댓글이 있으면 자식 목록에 추가
        if (parent != null) {
            parent.addChild(this);
        }
    }

    public void updateContent(String content) {
        validateContent(content);
        this.content = content;
        this.updatedAt = LocalDateTime.now();
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

    public boolean isReply() {
        return this.parent != null;
    }

    public boolean hasChildren() {
        return !this.children.isEmpty();
    }

    // 비즈니스 메서드 추가
    public boolean canEdit(User user) {
        return isAuthor(user) || user.isAdmin();
    }

    public boolean canDelete(User user) {
        return isAuthor(user) || user.isAdmin();
    }

    public int getDepth() {
        return isReply() ? 1 : 0; // 최대 2단계 (원댓글, 대댓글)
    }

    public int getChildrenCount() {
        return this.children.size();
    }

    public boolean canReply() {
        return !isReply(); // 대댓글에는 대댓글을 달 수 없음
    }

    private void addChild(Comment child) {
        this.children.add(child);
    }

    private void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("댓글 내용은 필수입니다");
        }
        if (content.length() > 1000) {
            throw new IllegalArgumentException("댓글 내용은 1000자를 초과할 수 없습니다");
        }
    }

    private void validatePost(Post post) {
        if (post == null) {
            throw new IllegalArgumentException("게시글은 필수입니다");
        }
        if (post.isDeleted()) {
            throw new IllegalArgumentException("삭제된 게시글에는 댓글을 작성할 수 없습니다");
        }
    }

    private void validateAuthor(User author) {
        if (author == null) {
            throw new IllegalArgumentException("작성자는 필수입니다");
        }
    }

    private void validateParentDepth(Comment parent) {
        if (parent != null && parent.isReply()) {
            throw new IllegalArgumentException("대댓글에는 대댓글을 작성할 수 없습니다");
        }
    }
}
