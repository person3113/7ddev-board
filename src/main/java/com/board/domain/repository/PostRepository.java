package com.board.domain.repository;

import com.board.domain.entity.Post;
import com.board.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 삭제되지 않은 게시글만 조회 (페이징)
     */
    Page<Post> findByDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 카테고리별 게시글 조회 (삭제되지 않은 것만)
     */
    Page<Post> findByCategoryAndDeletedFalseOrderByCreatedAtDesc(String category, Pageable pageable);

    /**
     * 작성자별 게시글 조회 (삭제되지 않은 것만)
     */
    Page<Post> findByAuthorAndDeletedFalseOrderByCreatedAtDesc(User author, Pageable pageable);

    /**
     * 제목으로 검색 (부분 일치, 삭제되지 않은 것만)
     */
    Page<Post> findByTitleContainingAndDeletedFalseOrderByCreatedAtDesc(String keyword, Pageable pageable);

    /**
     * 내용으로 검색 (부분 일치, 삭제되지 않은 것만)
     */
    Page<Post> findByContentContainingAndDeletedFalseOrderByCreatedAtDesc(String keyword, Pageable pageable);

    /**
     * 카테고리 목록 조회 (중복 제거)
     */
    @Query("SELECT DISTINCT p.category FROM Post p WHERE p.deleted = false AND p.category IS NOT NULL")
    List<String> findDistinctCategories();

    /**
     * 전체 게시글 수 (삭제되지 않은 것만)
     */
    long countByDeletedFalse();

    /**
     * 사용자명으로 게시글 조회 (삭제되지 않은 것만)
     */
    Page<Post> findByAuthorUsernameAndDeletedFalseOrderByCreatedAtDesc(String username, Pageable pageable);

    /**
     * 카테고리별 게시글 수 (삭제되지 않은 것만)
     */
    long countByCategoryAndDeletedFalse(String category);

    /**
     * 공지사항 조회 (삭제되지 않은 것만, 최신순)
     */
    Page<Post> findByIsNoticeTrueAndDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 일반 게시글 조회 (공지사항 제외, 삭제되지 않은 것만)
     */
    Page<Post> findByIsNoticeFalseAndDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 공지사항 개수 조회
     */
    long countByIsNoticeTrueAndDeletedFalse();

    /**
     * 조회수 기준으로 게시글 조회 (삭제되지 않은 것만, 페이징)
     */
    Page<Post> findByDeletedFalseOrderByViewCountDescCreatedAtDesc(Pageable pageable);

    /**
     * 좋아요 수 기준으로 게시글 조회 (삭제되지 않은 것만, 페이징)
     */
    Page<Post> findByDeletedFalseOrderByLikeCountDescCreatedAtDesc(Pageable pageable);

    /**
     * 작성자 닉네임으로 검색 (부분 일치, 삭제되지 않은 것만)
     */
    @Query("SELECT p FROM Post p JOIN p.author a WHERE a.nickname LIKE %:nickname% AND p.deleted = false ORDER BY p.createdAt DESC")
    Page<Post> findByAuthorNicknameContainingAndDeletedFalse(@Param("nickname") String nickname, Pageable pageable);

    /**
     * 제목 또는 내용으로 검색 (복합 검색, 삭제되지 않은 것만)
     */
    @Query("SELECT p FROM Post p WHERE (p.title LIKE %:keyword% OR p.content LIKE %:keyword%) AND p.deleted = false ORDER BY p.createdAt DESC")
    Page<Post> findByTitleContainingOrContentContainingAndDeletedFalse(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 모든 필드에서 검색 (제목, 내용, 카테고리, 작성자 닉네임)
     */
    @Query("SELECT p FROM Post p JOIN p.author a WHERE " +
           "(p.title LIKE %:keyword% OR p.content LIKE %:keyword% OR " +
           "p.category LIKE %:keyword% OR a.nickname LIKE %:keyword%) AND " +
           "p.deleted = false ORDER BY p.createdAt DESC")
    Page<Post> findByAllFieldsContaining(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 카테고리와 키워드로 검색 (제목 또는 내용에서)
     */
    @Query("SELECT p FROM Post p WHERE p.category = :category AND " +
           "(p.title LIKE %:keyword% OR p.content LIKE %:keyword%) AND " +
           "p.deleted = false ORDER BY p.createdAt DESC")
    Page<Post> findByCategoryAndKeyword(@Param("category") String category,
                                       @Param("keyword") String keyword,
                                       Pageable pageable);

    /**
     * 작성자와 키워드로 검색 (제목 또는 내용에서)
     */
    @Query("SELECT p FROM Post p JOIN p.author a WHERE a.nickname LIKE %:authorName% AND " +
           "(p.title LIKE %:keyword% OR p.content LIKE %:keyword%) AND " +
           "p.deleted = false ORDER BY p.createdAt DESC")
    Page<Post> findByAuthorAndKeyword(@Param("authorName") String authorName,
                                     @Param("keyword") String keyword,
                                     Pageable pageable);

    /**
     * 성능 최적화된 검색 메소드들
     */

    /**
     * 제목 검색 (LIKE 쿼리 최적화)
     */
    @Query("SELECT p FROM Post p WHERE " +
           "p.deleted = false AND " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY p.createdAt DESC")
    Page<Post> findByTitleContainingIgnoreCaseAndDeletedFalse(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 내용 검색 (FULLTEXT 검색 시뮬레이션)
     */
    @Query("SELECT p FROM Post p WHERE " +
           "p.deleted = false AND " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY p.createdAt DESC")
    Page<Post> findByContentContainingIgnoreCaseAndDeletedFalse(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 최적화된 복합 검색 (제목 + 내용)
     */
    @Query("SELECT p FROM Post p WHERE " +
           "p.deleted = false AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY p.createdAt DESC")
    Page<Post> findByTitleOrContentContainingIgnoreCaseAndDeletedFalse(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 최적화된 전체 검색 (모든 필드 + 댓글 내용 포함)
     */
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN p.author a LEFT JOIN p.comments c WHERE " +
           "p.deleted = false AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(a.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " (c.deleted = false AND LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%')))) " +
           "ORDER BY p.createdAt DESC")
    Page<Post> findByAllFieldsContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 카운트 쿼리 최적화 (검색 결과 수 조회)
     */
    @Query("SELECT COUNT(p) FROM Post p WHERE " +
           "p.deleted = false AND " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Long countByTitleContainingIgnoreCaseAndDeletedFalse(@Param("keyword") String keyword);

    @Query("SELECT COUNT(p) FROM Post p WHERE " +
           "p.deleted = false AND " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Long countByContentContainingIgnoreCaseAndDeletedFalse(@Param("keyword") String keyword);

    /**
     * 댓글 내용으로 게시글 검색 (최적화)
     */
    @Query("SELECT DISTINCT p FROM Post p JOIN p.comments c WHERE " +
           "p.deleted = false AND c.deleted = false AND " +
           "LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY p.createdAt DESC")
    Page<Post> findByCommentsContentContainingIgnoreCaseAndDeletedFalse(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 댓글 내용 검색 결과 카운트
     */
    @Query("SELECT COUNT(DISTINCT p) FROM Post p JOIN p.comments c WHERE " +
           "p.deleted = false AND c.deleted = false AND " +
           "LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Long countByCommentsContentContainingIgnoreCaseAndDeletedFalse(@Param("keyword") String keyword);
}
