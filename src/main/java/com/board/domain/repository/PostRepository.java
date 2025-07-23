package com.board.domain.repository;

import com.board.domain.entity.Post;
import com.board.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}
