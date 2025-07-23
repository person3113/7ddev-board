
#### 1. User 엔티티
```java
@Entity
@Table(name = "users", indexes = {@Index(name = "idx_username", columnList = "username")})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // enum: USER, ADMIN

    @Column(nullable = false)
    private LocalDateTime joinDate;

    private LocalDateTime lastLogin;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // Getter, Setter, Constructor
}
```

**설명**:
- `id`: `Long` 타입으로 PK 설정, 자동 증가.
- `username`, `email`: 고유 제약 조건 추가.
- `role`: 문자열 리터럴 대신 `enum` 사용.
- `joinDate`, `lastLogin`: `LocalDateTime`으로 변경해 시간 정보 정확히 관리.
- `posts`, `comments`: `@OneToMany`로 연관관계 설정. `mappedBy`로 양방향 관계 정의.
- `UserStats` 제거: 통계 데이터는 쿼리로 계산하거나, 캐싱(예: Redis)으로 처리해 중복 저장 방지.
- 인덱스: `username`에 인덱스 추가로 검색 성능 최적화.

#### 2. Post 엔티티
```java
@Entity
@Table(name = "posts", indexes = {@Index(name = "idx_category", columnList = "category")})
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private int views = 0;

    @Column(nullable = false)
    private int likes = 0;

    @Column(nullable = false)
    private int dislikes = 0;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false)
    private boolean isNotice = false;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // Getter, Setter, Constructor
}
```

**설명**:
- `author`: `User` 엔티티와 `@ManyToOne` 관계로 변경. 외래 키는 `author_id`.
- `createdAt`: `LocalDateTime`으로 변경.
- `views`, `likes`, `dislikes`: `int` 타입으로 명확히 정의, 기본값 0.
- `category`: 검색이 빈번할 가능성이 있으므로 인덱스 추가.
- `isDeleted`: 소프트 삭제를 위해 유지.
- `comments`: `@OneToMany`로 연관관계 설정.

#### 3. Comment 엔티티
```java
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private int likes = 0;

    @Column(nullable = false)
    private boolean isDeleted = false;

    // Getter, Setter, Constructor
}
```

**설명**:
- `postId`, `author`: 각각 `Post`와 `User` 엔티티로 `@ManyToOne` 연관관계 설정.
- `createdAt`: `LocalDateTime`으로 변경.
- `isDeleted`: 소프트 삭제 유지.
- `content`: `TEXT` 타입으로 긴 댓글 지원.

#### 4. ReportedPost 엔티티
```java
@Entity
@Table(name = "reported_posts")
public class ReportedPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false)
    private int reportCount = 0;

    @ElementCollection
    @CollectionTable(name = "reported_post_reasons", joinColumns = @JoinColumn(name = "reported_post_id"))
    @Column(name = "reason")
    private List<String> reportReasons = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.PENDING; // enum: PENDING, RESOLVED, DISMISSED

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Getter, Setter, Constructor
}
```

**설명**:
- `title`, `author`, `category` 제거: `Post` 엔티티와 연관관계로 중복 제거.
- `reportReasons`: `@ElementCollection`으로 간단한 리스트 관리.
- `status`: `enum` 타입으로 명확히 정의.
- `createdAt`: `LocalDateTime`으로 변경.

#### 5. ReportedComment 엔티티
```java
@Entity
@Table(name = "reported_comments")
public class ReportedComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @Column(nullable = false)
    private int reportCount = 0;

    @ElementCollection
    @CollectionTable(name = "reported_comment_reasons", joinColumns = @JoinColumn(name = "reported_comment_id"))
    @Column(name = "reason")
    private List<String> reportReasons = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.PENDING; // enum: PENDING, RESOLVED, DISMISSED

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Getter, Setter, Constructor
}
```

**설명**:
- `content`, `author`, `postTitle` 제거: `Comment` 엔티티와 연관관계로 중복 제거.
- `reportReasons`, `status`, `createdAt`: 동일하게 처리.

#### Enum 클래스
```java
public enum Role {
    USER, ADMIN
}

public enum ReportStatus {
    PENDING, RESOLVED, DISMISSED
}
```

---

### 주요 개선 포인트
1. **정규화**:
    - `UserStats` 제거: 통계는 `Post`와 `Comment`를 쿼리로 집계하거나 캐싱 처리.
    - `ReportedPost`, `ReportedComment`에서 중복 필드 제거 (`title`, `author` 등은 원본 엔티티에서 참조).
2. **연관관계**:
    - `@ManyToOne`, `@OneToMany`로 명확한 관계 설정.
    - `FetchType.LAZY`로 성능 최적화 (필요 시 데이터만 로드).
    - `cascade`와 `orphanRemoval`로 데이터 무결성 유지.
3. **데이터 타입**:
    - `LocalDateTime`으로 시간 정보 정확히 관리.
    - `enum`으로 상태 값 관리 (`Role`, `ReportStatus`).
4. **인덱스**:
    - 자주 검색되는 필드(`username`, `category`)에 인덱스 추가.
5. **소프트 삭제**:
    - `isDeleted` 플래그로 삭제된 데이터 관리.
6. **실무적 고려**:
    - 페이징 처리: `Post`와 `Comment` 목록 조회 시 `@Pageable` 사용 권장.
    - 캐싱: 통계 데이터는 Redis나 쿼리 캐싱으로 처리.
    - 트랜잭션: 신고 처리(`ReportedPost`, `ReportedComment`)는 트랜잭션 관리 필요.

