package com.board.service;

import com.board.domain.entity.Post;
import com.board.domain.entity.User;
import com.board.domain.repository.PostRepository;
import com.board.domain.repository.UserRepository;
import com.board.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ViewServiceTest {

    @Autowired
    private ViewService viewService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

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
    @DisplayName("게시글 첫 조회 시 조회수 증가")
    void increaseViewCount_FirstView_Success() {
        // given
        Long postId = post.getId();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);

        int initialViewCount = post.getViewCount();

        // when
        viewService.increaseViewCount(postId, request);

        // then
        Post updatedPost = postRepository.findById(postId).orElse(null);
        assertThat(updatedPost).isNotNull();
        assertThat(updatedPost.getViewCount()).isEqualTo(initialViewCount + 1);

        // 세션에 조회 기록이 저장되었는지 확인
        String sessionKey = "viewed_post_" + postId;
        assertThat(session.getAttribute(sessionKey)).isNotNull();
    }

    @Test
    @DisplayName("같은 세션에서 중복 조회 시 조회수 증가하지 않음")
    void increaseViewCount_DuplicateView_NoIncrease() {
        // given
        Long postId = post.getId();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);

        // 첫 번째 조회
        viewService.increaseViewCount(postId, request);
        int viewCountAfterFirst = postRepository.findById(postId).get().getViewCount();

        // when - 같은 세션에서 두 번째 조회
        viewService.increaseViewCount(postId, request);

        // then
        Post updatedPost = postRepository.findById(postId).orElse(null);
        assertThat(updatedPost).isNotNull();
        assertThat(updatedPost.getViewCount()).isEqualTo(viewCountAfterFirst); // 증가하지 않음
    }

    @Test
    @DisplayName("다른 세션에서 조회 시 각각 조회수 증가")
    void increaseViewCount_DifferentSessions_BothIncrease() {
        // given
        Long postId = post.getId();

        // 첫 번째 세션
        MockHttpServletRequest request1 = new MockHttpServletRequest();
        MockHttpSession session1 = new MockHttpSession();
        request1.setSession(session1);

        // 두 번째 세션
        MockHttpServletRequest request2 = new MockHttpServletRequest();
        MockHttpSession session2 = new MockHttpSession();
        request2.setSession(session2);

        int initialViewCount = post.getViewCount();

        // when
        viewService.increaseViewCount(postId, request1);
        viewService.increaseViewCount(postId, request2);

        // then
        Post updatedPost = postRepository.findById(postId).orElse(null);
        assertThat(updatedPost).isNotNull();
        assertThat(updatedPost.getViewCount()).isEqualTo(initialViewCount + 2);
    }

    @Test
    @DisplayName("세션이 없는 요청에서도 조회수 증가")
    void increaseViewCount_NoSession_Success() {
        // given
        Long postId = post.getId();
        MockHttpServletRequest request = new MockHttpServletRequest();
        // 세션을 설정하지 않음

        int initialViewCount = post.getViewCount();

        // when
        viewService.increaseViewCount(postId, request);

        // then
        Post updatedPost = postRepository.findById(postId).orElse(null);
        assertThat(updatedPost).isNotNull();
        assertThat(updatedPost.getViewCount()).isEqualTo(initialViewCount + 1);
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 시 예외")
    void increaseViewCount_NotFoundPost_ThrowException() {
        // given
        Long nonExistentPostId = 999L;
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);

        // when & then
        assertThatThrownBy(() -> viewService.increaseViewCount(nonExistentPostId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("게시글을 찾을 수 없습니다: " + nonExistentPostId);
    }

    @Test
    @DisplayName("세션 만료 후 재조회 시 조회수 증가")
    void increaseViewCount_SessionExpired_Success() {
        // given
        Long postId = post.getId();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);

        // 첫 번째 조회
        viewService.increaseViewCount(postId, request);
        int viewCountAfterFirst = postRepository.findById(postId).get().getViewCount();

        // 세션 무효화 (만료 시뮬레이션)
        session.invalidate();
        MockHttpSession newSession = new MockHttpSession();
        request.setSession(newSession);

        // when - 새 세션에서 조회
        viewService.increaseViewCount(postId, request);

        // then
        Post updatedPost = postRepository.findById(postId).orElse(null);
        assertThat(updatedPost).isNotNull();
        assertThat(updatedPost.getViewCount()).isEqualTo(viewCountAfterFirst + 1);
    }
}
