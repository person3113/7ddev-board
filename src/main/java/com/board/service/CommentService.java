package com.board.service;

import com.board.domain.entity.Comment;
import com.board.domain.entity.Post;
import com.board.domain.entity.User;
import com.board.domain.repository.CommentRepository;
import com.board.domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    /**
     * 댓글 생성
     */
    public Comment createComment(Long postId, String content, User author) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        if (post.isDeleted()) {
            throw new IllegalArgumentException("삭제된 게시글에는 댓글을 작성할 수 없습니다");
        }

        Comment comment = Comment.builder()
                .content(content)
                .post(post)
                .author(author)
                .build();

        return commentRepository.save(comment);
    }

    /**
     * 대댓글 생성
     */
    public Comment createReply(Long parentCommentId, String content, User author) {
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다"));

        if (parentComment.isDeleted()) {
            throw new IllegalArgumentException("삭제된 댓글에는 대댓글을 작성할 수 없습니다");
        }

        if (parentComment.isReply()) {
            throw new IllegalArgumentException("대댓글에는 대댓글을 작성할 수 없습니다");
        }

        Comment reply = Comment.builder()
                .content(content)
                .post(parentComment.getPost())
                .author(author)
                .parent(parentComment)
                .build();

        return commentRepository.save(reply);
    }

    /**
     * 댓글 ID로 조회
     */
    @Transactional(readOnly = true)
    public Comment findById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다"));

        if (comment.isDeleted()) {
            throw new IllegalArgumentException("삭제된 댓글입니다");
        }

        return comment;
    }

    /**
     * 게시글별 댓글 목록 조회 (최상위 댓글만, 페이징)
     */
    @Transactional(readOnly = true)
    public Page<Comment> findCommentsByPost(Long postId, Pageable pageable) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        return commentRepository.findByPostAndParentIsNullAndDeletedFalseOrderByCreatedAtAsc(post, pageable);
    }

    /**
     * 게시글별 모든 댓글 조회 (대댓글 포함, 페이징)
     */
    @Transactional(readOnly = true)
    public Page<Comment> findAllCommentsByPost(Long postId, Pageable pageable) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        return commentRepository.findByPostAndDeletedFalseOrderByCreatedAtAsc(post, pageable);
    }

    /**
     * 게시글별 모든 댓글 조회 (삭제된 댓글도 포함, 대댓글 포함, 페이징)
     */
    @Transactional(readOnly = true)
    public Page<Comment> getCommentsByPost(Long postId, Pageable pageable) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        return commentRepository.findByPostOrderByCreatedAtAsc(post, pageable);
    }

    /**
     * 댓글 수정
     */
    public Comment updateComment(Long commentId, String newContent, User user) {
        Comment comment = findById(commentId);

        if (!comment.canEdit(user)) {
            throw new IllegalArgumentException("댓글 수정 권한이 없습니다");
        }

        comment.updateContent(newContent);
        return commentRepository.save(comment);
    }

    /**
     * 댓글 삭제 (소프트 삭제)
     */
    public void deleteComment(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다"));

        if (comment.isDeleted()) {
            throw new IllegalArgumentException("이미 삭제된 댓글입니다");
        }

        if (!comment.canDelete(user)) {
            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다");
        }

        comment.delete();
        commentRepository.save(comment);
    }

    /**
     * 댓글 좋아요 증가
     */
    public void increaseLikeCount(Long commentId) {
        Comment comment = findById(commentId);
        comment.increaseLikeCount();
        commentRepository.save(comment);
    }

    /**
     * 댓글 좋아요 감소
     */
    public void decreaseLikeCount(Long commentId) {
        Comment comment = findById(commentId);
        comment.decreaseLikeCount();
        commentRepository.save(comment);
    }

    /**
     * 게시글의 댓글 수 조회
     */
    @Transactional(readOnly = true)
    public long getCommentCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        return commentRepository.countByPostAndDeletedFalse(post);
    }

    /**
     * 사용자별 댓글 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<Comment> findCommentsByAuthor(User author, Pageable pageable) {
        return commentRepository.findByAuthorAndDeletedFalseOrderByCreatedAtDesc(author, pageable);
    }
}
