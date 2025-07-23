package com.board.domain.repository;

import com.board.domain.entity.Comment;
import com.board.domain.entity.Post;
import com.board.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 특정 게시글의 댓글 조회 (삭제되지 않은 것만, 페이징)
     */
    Page<Comment> findByPostAndDeletedFalseOrderByCreatedAtAsc(Post post, Pageable pageable);

    /**
     * 특정 게시글의 최상위 댓글만 조회 (대댓글 제외)
     */
    Page<Comment> findByPostAndParentIsNullAndDeletedFalseOrderByCreatedAtAsc(Post post, Pageable pageable);

    /**
     * 특정 댓글의 대댓글 조회
     */
    List<Comment> findByParentAndDeletedFalseOrderByCreatedAtAsc(Comment parent);

    /**
     * 작성자별 댓글 조회 (삭제되지 않은 것만)
     */
    Page<Comment> findByAuthorAndDeletedFalseOrderByCreatedAtDesc(User author, Pageable pageable);

    /**
     * 댓글 내용으로 검색 (부분 일치, 삭제되지 않은 것만)
     */
    Page<Comment> findByContentContainingAndDeletedFalseOrderByCreatedAtDesc(String keyword, Pageable pageable);

    /**
     * 특정 게시글의 댓글 수 조회 (삭제되지 않은 것만)
     */
    long countByPostAndDeletedFalse(Post post);

    /**
     * 특정 게시글의 최상위 댓글 수 조회 (대댓글 제외)
     */
    long countByPostAndParentIsNullAndDeletedFalse(Post post);

    /**
     * 특정 댓글의 대댓글 수 조회
     */
    long countByParentAndDeletedFalse(Comment parent);

    /**
     * 사용자별 댓글 수 조회 (삭제되지 않은 것만)
     */
    long countByAuthorAndDeletedFalse(User author);

    /**
     * 최근 댓글 조회 (전체, 삭제되지 않은 것만)
     */
    Page<Comment> findByDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 인기 댓글 조회 (좋아요 수 기준)
     */
    Page<Comment> findByDeletedFalseOrderByLikeCountDescCreatedAtDesc(Pageable pageable);
}
