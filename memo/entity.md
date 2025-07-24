# 엔티티 설계 가이드라인

## 현재 구현된 엔티티

### 1. User 엔티티
```java
@Entity
@Table(name = "users", indexes = {@Index(name = "idx_username", columnList = "username")})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // enum: USER, ADMIN

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime lastLogin;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    // 비즈니스 로직 메서드들 포함
}
```

### 2. Post 엔티티
```java
@Entity
@Table(name = "posts", indexes = {
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_created_at", columnList = "createdAt"),
    @Index(name = "idx_deleted", columnList = "deleted"),
    @Index(name = "idx_notice", columnList = "notice")
})
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

    @Column(nullable = false)
    private Boolean notice = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();
}
```

### 3. Comment 엔티티
```java
@Entity
@Table(name = "comments", indexes = {
    @Index(name = "idx_post_id", columnList = "post_id"),
    @Index(name = "idx_created_at", columnList = "createdAt"),
    @Index(name = "idx_deleted", columnList = "deleted"),
    @Index(name = "idx_parent_id", columnList = "parent_id")
})
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

    // 대댓글 기능
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

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

### 4. Role Enum
```java
public enum Role {
    USER, ADMIN
}
```

## 설계 가이드라인

### 공통 원칙
- **Spring Data JPA Auditing** 사용 (`@CreatedDate`, `@LastModifiedDate`)
- **지연 로딩** (`FetchType.LAZY`) 기본 사용
- **소프트 삭제** (`deleted` 플래그)
- **인덱스** 최적화: 자주 조회되는 필드에 인덱스 설정

### 연관관계 설정
- `@ManyToOne`: `FetchType.LAZY` 사용
- `@OneToMany`: `mappedBy`, `cascade`, `orphanRemoval` 적절히 설정
- 양방향 연관관계는 필요한 경우에만 사용

### 데이터 무결성
- `@Column(nullable = false)`: 필수 필드는 NOT NULL 제약
- `unique = true`: 고유 제약이 필요한 필드에 설정
- `length`: 문자열 필드는 적절한 길이 제한

### 성능 최적화
- 조회가 빈번한 필드에 인덱스 추가
- `@ToString(exclude = {...})`: 순환 참조 방지
- 비즈니스 로직은 엔티티 내부에 캡슐화

## 향후 구현 예정 엔티티

### ReportedPost 엔티티 (신고된 게시글)
```java
@Entity
@Table(name = "reported_posts", indexes = {
    @Index(name = "idx_post_id", columnList = "post_id"),
    @Index(name = "idx_reporter_id", columnList = "reporter_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
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
    private ReportStatus status; // PENDING, APPROVED, REJECTED

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

### ReportedComment 엔티티 (신고된 댓글)
```java
@Entity
@Table(name = "reported_comments", indexes = {
    @Index(name = "idx_comment_id", columnList = "comment_id"),
    @Index(name = "idx_reporter_id", columnList = "reporter_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
public class ReportedComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status; // PENDING, APPROVED, REJECTED

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

### ReportStatus Enum
```java
public enum ReportStatus {
    PENDING,    // 대기중
    APPROVED,   // 승인됨 (신고 처리 완료)
    REJECTED    // 거부됨 (신고 반려)
}
```
