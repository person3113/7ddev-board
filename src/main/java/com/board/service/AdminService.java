package com.board.service;

import com.board.domain.entity.Comment;
import com.board.domain.entity.Post;
import com.board.domain.entity.User;
import com.board.domain.enums.Role;
import com.board.domain.repository.CommentRepository;
import com.board.domain.repository.PostRepository;
import com.board.domain.repository.UserRepository;
import com.board.exception.ResourceNotFoundException;
import com.board.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    /**
     * 관리자 권한 확인
     */
    private void validateAdminRole(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + username));
        
        if (user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("관리자 권한이 필요합니다");
        }
    }

    /**
     * 게시글 강제 삭제
     */
    @Transactional
    public void forceDeletePost(Long postId, String adminUsername) {
        validateAdminRole(adminUsername);
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다: " + postId));
        
        post.delete();
        postRepository.save(post);
    }

    /**
     * 댓글 강제 삭제
     */
    @Transactional
    public void forceDeleteComment(Long commentId, String adminUsername) {
        validateAdminRole(adminUsername);
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("댓글을 찾을 수 없습니다: " + commentId));
        
        comment.delete();
        commentRepository.save(comment);
    }

    /**
     * 사용자 권한 변경
     */
    @Transactional
    public void changeUserRole(String targetUsername, Role newRole, String adminUsername) {
        validateAdminRole(adminUsername);
        
        User targetUser = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + targetUsername));
        
        targetUser.changeRole(newRole);
        userRepository.save(targetUser);
    }

    /**
     * 모든 게시글 조회 (삭제된 것 포함)
     */
    public Page<Post> getAllPosts(String adminUsername, Pageable pageable) {
        validateAdminRole(adminUsername);
        
        return postRepository.findAll(pageable);
    }

    /**
     * 모든 댓글 조회 (삭제된 것 포함)
     */
    public Page<Comment> getAllComments(String adminUsername, Pageable pageable) {
        validateAdminRole(adminUsername);
        
        return commentRepository.findAll(pageable);
    }

    /**
     * 모든 사용자 조회
     */
    public Page<User> getAllUsers(String adminUsername, Pageable pageable) {
        validateAdminRole(adminUsername);
        
        return userRepository.findAll(pageable);
    }

    /**
     * 관리자 대시보드 통계 정보 조회
     */
    public AdminStats getAdminStats(String adminUsername) {
        validateAdminRole(adminUsername);
        
        long totalUsers = userRepository.count();
        long totalPosts = postRepository.count();
        long activePosts = postRepository.countByDeletedFalse();
        long totalComments = commentRepository.count();
        long activeComments = commentRepository.findByDeletedFalseOrderByCreatedAtDesc(Pageable.unpaged()).getTotalElements();
        
        return new AdminStats(totalUsers, totalPosts, activePosts, totalComments, activeComments);
    }

    /**
     * 관리자 통계 정보를 담는 내부 클래스
     */
    public static class AdminStats {
        private final long totalUsers;
        private final long totalPosts;
        private final long activePosts;
        private final long totalComments;
        private final long activeComments;

        public AdminStats(long totalUsers, long totalPosts, long activePosts, long totalComments, long activeComments) {
            this.totalUsers = totalUsers;
            this.totalPosts = totalPosts;
            this.activePosts = activePosts;
            this.totalComments = totalComments;
            this.activeComments = activeComments;
        }

        public long getTotalUsers() { return totalUsers; }
        public long getTotalPosts() { return totalPosts; }
        public long getActivePosts() { return activePosts; }
        public long getDeletedPosts() { return totalPosts - activePosts; }
        public long getTotalComments() { return totalComments; }
        public long getActiveComments() { return activeComments; }
        public long getDeletedComments() { return totalComments - activeComments; }
    }
}
