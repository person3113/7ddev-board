package com.board.service;

import com.board.domain.entity.Comment;
import com.board.domain.entity.CommentLike;
import com.board.domain.entity.User;
import com.board.domain.repository.CommentLikeRepository;
import com.board.domain.repository.CommentRepository;
import com.board.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    /**
     * 댓글 좋아요 토글 (좋아요/좋아요 취소)
     * @param commentId 댓글 ID
     * @param userId 사용자 ID
     * @return true: 좋아요 추가, false: 좋아요 취소
     */
    @Transactional
    public boolean toggleLike(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 기존 좋아요 확인
        return commentLikeRepository.findByCommentAndUser(comment, user)
                .map(existingLike -> {
                    // 이미 좋아요가 있으면 삭제 (좋아요 취소)
                    commentLikeRepository.delete(existingLike);
                    comment.decreaseLikeCount();
                    log.info("댓글 좋아요 취소: commentId={}, userId={}", commentId, userId);
                    return false;
                })
                .orElseGet(() -> {
                    // 좋아요가 없으면 생성 (좋아요 추가)
                    CommentLike commentLike = CommentLike.builder()
                            .comment(comment)
                            .user(user)
                            .build();
                    commentLikeRepository.save(commentLike);
                    comment.increaseLikeCount();
                    log.info("댓글 좋아요 추가: commentId={}, userId={}", commentId, userId);
                    return true;
                });
    }

    /**
     * 댓글의 좋아요 개수 조회
     */
    @Transactional(readOnly = true)
    public long getLikeCount(Long commentId) {
        return commentLikeRepository.countByCommentId(commentId);
    }

    /**
     * 특정 사용자가 댓글에 좋아요를 눌렀는지 확인
     */
    @Transactional(readOnly = true)
    public boolean isLikedByUser(Long commentId, Long userId) {
        return commentLikeRepository.existsByCommentIdAndUserId(commentId, userId);
    }
}
