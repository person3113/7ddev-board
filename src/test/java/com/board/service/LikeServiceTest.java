package com.board.service;

import com.board.domain.entity.Post;
import com.board.domain.entity.PostLike;
import com.board.domain.entity.User;
import com.board.domain.repository.PostLikeRepository;
import com.board.domain.repository.PostRepository;
import com.board.domain.repository.UserRepository;
import com.board.exception.ResourceNotFoundException;
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
class LikeServiceTest {

    @Autowired
    private LikeService likeService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    private User user;
    private Post post;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .nickname("테스트유저")
                .build();
        user = userRepository.save(user);

        post = Post.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .author(user)
                .build();
        post = postRepository.save(post);
    }

    @Test
    @DisplayName("게시글 추천 - 첫 추천")
    void likePost_FirstTime_Success() {
        // given
        Long postId = post.getId();
        Long userId = user.getId();

        // when
        likeService.likePost(postId, userId);

        // then
        PostLike postLike = postLikeRepository.findByPostAndUser(post, user).orElse(null);
        assertThat(postLike).isNotNull();
        assertThat(postLike.getIsLike()).isTrue();

        // 게시글의 추천 수가 증가했는지 확인
        Post updatedPost = postRepository.findById(postId).orElse(null);
        assertThat(updatedPost).isNotNull();
        assertThat(updatedPost.getLikeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("게시글 비추천 - 첫 비추천")
    void dislikePost_FirstTime_Success() {
        // given
        Long postId = post.getId();
        Long userId = user.getId();

        // when
        likeService.dislikePost(postId, userId);

        // then
        PostLike postLike = postLikeRepository.findByPostAndUser(post, user).orElse(null);
        assertThat(postLike).isNotNull();
        assertThat(postLike.getIsLike()).isFalse();

        // 게시글의 추천 수는 변하지 않음
        Post updatedPost = postRepository.findById(postId).orElse(null);
        assertThat(updatedPost).isNotNull();
        assertThat(updatedPost.getLikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("게시글 추천 취소")
    void cancelLike_Success() {
        // given - 먼저 추천
        Long postId = post.getId();
        Long userId = user.getId();
        likeService.likePost(postId, userId);

        // when - 추천 취소
        likeService.cancelLike(postId, userId);

        // then
        PostLike postLike = postLikeRepository.findByPostAndUser(post, user).orElse(null);
        assertThat(postLike).isNull();

        // 게시글의 추천 수가 감소했는지 확인
        Post updatedPost = postRepository.findById(postId).orElse(null);
        assertThat(updatedPost).isNotNull();
        assertThat(updatedPost.getLikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("추천에서 비추천으로 변경")
    void changeLikeToDislike_Success() {
        // given - 먼저 추천
        Long postId = post.getId();
        Long userId = user.getId();
        likeService.likePost(postId, userId);

        // when - 비추천으로 변경
        likeService.dislikePost(postId, userId);

        // then
        PostLike postLike = postLikeRepository.findByPostAndUser(post, user).orElse(null);
        assertThat(postLike).isNotNull();
        assertThat(postLike.getIsLike()).isFalse();

        // 게시글의 추천 수가 감소했는지 확인
        Post updatedPost = postRepository.findById(postId).orElse(null);
        assertThat(updatedPost).isNotNull();
        assertThat(updatedPost.getLikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("비추천에서 추천으로 변경")
    void changeDislikeToLike_Success() {
        // given - 먼저 비추천
        Long postId = post.getId();
        Long userId = user.getId();
        likeService.dislikePost(postId, userId);

        // when - 추천으로 변경
        likeService.likePost(postId, userId);

        // then
        PostLike postLike = postLikeRepository.findByPostAndUser(post, user).orElse(null);
        assertThat(postLike).isNotNull();
        assertThat(postLike.getIsLike()).isTrue();

        // 게시글의 추천 수가 증가했는지 확인
        Post updatedPost = postRepository.findById(postId).orElse(null);
        assertThat(updatedPost).isNotNull();
        assertThat(updatedPost.getLikeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("존재하지 않는 게시글 추천 시 예외")
    void likePost_NotFoundPost_ThrowException() {
        // given
        Long nonExistentPostId = 999L;
        Long userId = user.getId();

        // when & then
        assertThatThrownBy(() -> likeService.likePost(nonExistentPostId, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("게시글을 찾을 수 없습니다: " + nonExistentPostId);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 추천 시 예외")
    void likePost_NotFoundUser_ThrowException() {
        // given
        Long postId = post.getId();
        Long nonExistentUserId = 999L;

        // when & then
        assertThatThrownBy(() -> likeService.likePost(postId, nonExistentUserId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다: " + nonExistentUserId);
    }

    @Test
    @DisplayName("사용자 추천 상태 조회")
    void getUserLikeStatus_Success() {
        // given
        Long postId = post.getId();
        Long userId = user.getId();

        // when - 추천하지 않은 상태
        Boolean status1 = likeService.getUserLikeStatus(postId, userId);

        // 추천한 상태
        likeService.likePost(postId, userId);
        Boolean status2 = likeService.getUserLikeStatus(postId, userId);

        // 비추천한 상태
        likeService.dislikePost(postId, userId);
        Boolean status3 = likeService.getUserLikeStatus(postId, userId);

        // then
        assertThat(status1).isNull(); // 추천/비추천 하지 않음
        assertThat(status2).isTrue(); // 추천
        assertThat(status3).isFalse(); // 비추천
    }
}
