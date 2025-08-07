package com.board.service;

import com.board.domain.entity.Comment;
import com.board.domain.entity.Post;
import com.board.domain.entity.User;
import com.board.domain.repository.CommentRepository;
import com.board.domain.repository.PostRepository;
import com.board.domain.repository.UserRepository;
import com.board.exception.ResourceNotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    /**
     * 사용자 프로필 정보 조회
     */
    public UserProfile getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + username));

        return new UserProfile(user);
    }

    /**
     * 사용자의 게시글 목록 조회 (페이징)
     */
    public Page<Post> getUserPosts(String username, Pageable pageable) {
        return postRepository.findByAuthorUsernameAndDeletedFalseOrderByCreatedAtDesc(username, pageable);
    }

    /**
     * 사용자의 댓글 목록 조회 (페이징)
     */
    public Page<Comment> getUserComments(String username, Pageable pageable) {
        return commentRepository.findByAuthorUsernameAndDeletedFalseOrderByCreatedAtDesc(username, pageable);
    }

    /**
     * 사용자 통계 정보 조회
     */
    public UserStats getUserStats(String username) {
        // 사용자 존재 확인
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + username));

        long postCount = postRepository.findByAuthorUsernameAndDeletedFalseOrderByCreatedAtDesc(username, Pageable.unpaged()).getTotalElements();
        long commentCount = commentRepository.findByAuthorUsernameAndDeletedFalseOrderByCreatedAtDesc(username, Pageable.unpaged()).getTotalElements();

        return new UserStats(postCount, commentCount);
    }

    /**
     * 사용자 프로필 정보를 담는 내부 클래스
     */
    @Getter
    @RequiredArgsConstructor
    public static class UserProfile {
        private final User user;

        public String getUsername() {
            return user.getUsername();
        }

        public String getNickname() {
            return user.getNickname();
        }

        public String getEmail() {
            return user.getEmail();
        }

        public String getRole() {
            return user.getRole().name();
        }

        public String getCreatedAt() {
            return user.getCreatedAt().toString();
        }

        public String getLastLogin() {
            return user.getLastLogin() != null ? user.getLastLogin().toString() : null;
        }
    }

    /**
     * 사용자 통계 정보를 담는 내부 클래스
     */
    @Getter
    @RequiredArgsConstructor
    public static class UserStats {
        private final long postCount;
        private final long commentCount;

        public long getTotalActivityCount() {
            return postCount + commentCount;
        }
    }
}
