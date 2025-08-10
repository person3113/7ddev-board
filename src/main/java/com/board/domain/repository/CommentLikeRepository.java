package com.board.domain.repository;

import com.board.domain.entity.Comment;
import com.board.domain.entity.CommentLike;
import com.board.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    /**
     * 특정 댓글에 대한 특정 사용자의 좋아요 조회
     */
    Optional<CommentLike> findByCommentAndUser(Comment comment, User user);

    /**
     * 특정 댓글의 좋아요 개수 조회
     */
    @Query("SELECT COUNT(cl) FROM CommentLike cl WHERE cl.comment.id = :commentId")
    long countByCommentId(@Param("commentId") Long commentId);

    /**
     * 특정 댓글에 대한 특정 사용자의 좋아요 존재 여부 확인
     */
    @Query("SELECT COUNT(cl) > 0 FROM CommentLike cl WHERE cl.comment.id = :commentId AND cl.user.id = :userId")
    boolean existsByCommentIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);

    /**
     * 특정 댓글에 대한 모든 좋아요 삭제 (댓글 삭제 시 사용)
     */
    void deleteByComment(Comment comment);

    /**
     * 특정 사용자의 모든 댓글 좋아요 삭제 (사용자 탈퇴 시 사용)
     */
    void deleteByUser(User user);
}
