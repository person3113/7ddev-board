package com.board.service;

import com.board.domain.entity.Comment;
import com.board.domain.entity.CommentLike;
import com.board.domain.entity.Post;
import com.board.domain.entity.User;
import com.board.domain.repository.CommentLikeRepository;
import com.board.domain.repository.CommentRepository;
import com.board.domain.repository.PostRepository;
import com.board.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CommentLikeServiceTest {

    @Autowired
    private CommentLikeService commentLikeService;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Post post;
    private Comment comment;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .nickname("테스트유저")
                .build();
        user = userRepository.save(user);

        // 테스트 게시글 생성
        post = Post.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .author(user)
                .build();
        post = postRepository.save(post);

        // 테스트 댓글 생성
        comment = Comment.builder()
                .content("테스트 댓글")
                .post(post)
                .author(user)
                .build();
        comment = commentRepository.save(comment);
    }

    @Test
    @DisplayName("댓글 좋아요를 누를 수 있다")
    void canLikeComment() {
        // given
        Long commentId = comment.getId();
        Long userId = user.getId();

        // when
        boolean result = commentLikeService.toggleLike(commentId, userId);

        // then
        assertThat(result).isTrue(); // 좋아요 추가됨
        assertThat(commentLikeService.getLikeCount(commentId)).isEqualTo(1);
        assertThat(commentLikeService.isLikedByUser(commentId, userId)).isTrue();
    }

    @Test
    @DisplayName("이미 좋아요한 댓글은 좋아요를 취소할 수 있다")
    void canUnlikeComment() {
        // given
        Long commentId = comment.getId();
        Long userId = user.getId();
        commentLikeService.toggleLike(commentId, userId); // 먼저 좋아요

        // when
        boolean result = commentLikeService.toggleLike(commentId, userId);

        // then
        assertThat(result).isFalse(); // 좋아요 취소됨
        assertThat(commentLikeService.getLikeCount(commentId)).isEqualTo(0);
        assertThat(commentLikeService.isLikedByUser(commentId, userId)).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 댓글에 좋아요를 누르면 예외가 발생한다")
    void throwsExceptionWhenCommentNotFound() {
        // given
        Long nonExistentCommentId = 999L;
        Long userId = user.getId();

        // when & then
        assertThatThrownBy(() -> commentLikeService.toggleLike(nonExistentCommentId, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("댓글을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("존재하지 않는 사용자가 좋아요를 누르면 예외가 발생한다")
    void throwsExceptionWhenUserNotFound() {
        // given
        Long commentId = comment.getId();
        Long nonExistentUserId = 999L;

        // when & then
        assertThatThrownBy(() -> commentLikeService.toggleLike(commentId, nonExistentUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자를 찾을 수 없습니다");
    }
}
