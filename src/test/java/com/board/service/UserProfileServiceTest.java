package com.board.service;

import com.board.domain.entity.Comment;
import com.board.domain.entity.Post;
import com.board.domain.entity.User;
import com.board.domain.enums.Role;
import com.board.domain.repository.CommentRepository;
import com.board.domain.repository.PostRepository;
import com.board.domain.repository.UserRepository;
import com.board.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserProfileServiceTest {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .nickname("테스트유저")
                .role(Role.USER)
                .build();
        testUser = userRepository.save(testUser);

        otherUser = User.builder()
                .username("otheruser")
                .email("other@example.com")
                .password("password123")
                .nickname("다른유저")
                .role(Role.USER)
                .build();
        otherUser = userRepository.save(otherUser);

        // 테스트 게시글 생성
        for (int i = 1; i <= 5; i++) {
            Post post = Post.builder()
                    .title("제목 " + i)
                    .content("내용 " + i)
                    .category("카테고리")
                    .author(testUser)
                    .build();
            postRepository.save(post);
        }

        // 다른 사용자의 게시글에 댓글 생성
        Post otherPost = Post.builder()
                .title("다른 사용자 게시글")
                .content("다른 사용자 내용")
                .category("카테고리")
                .author(otherUser)
                .build();
        otherPost = postRepository.save(otherPost);

        for (int i = 1; i <= 3; i++) {
            Comment comment = Comment.builder()
                    .content("댓글 " + i)
                    .post(otherPost)
                    .author(testUser)
                    .build();
            commentRepository.save(comment);
        }
    }

    @Test
    @DisplayName("존재하는 사용자의 프로필을 조회할 수 있다")
    void getUserProfile_Success() {
        // when
        UserProfileService.UserProfile profile = userProfileService.getUserProfile("testuser");

        // then
        assertThat(profile).isNotNull();
        assertThat(profile.getUser().getUsername()).isEqualTo("testuser");
        assertThat(profile.getUser().getNickname()).isEqualTo("테스트유저");
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 예외가 발생한다")
    void getUserProfile_UserNotFound() {
        // when & then
        assertThatThrownBy(() -> userProfileService.getUserProfile("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("사용자의 게시글 목록을 페이징으로 조회할 수 있다")
    void getUserPosts_WithPaging() {
        // given
        Pageable pageable = PageRequest.of(0, 3);

        // when
        Page<Post> posts = userProfileService.getUserPosts("testuser", pageable);

        // then
        assertThat(posts).isNotNull();
        assertThat(posts.getContent()).hasSize(3);
        assertThat(posts.getTotalElements()).isEqualTo(5);
        assertThat(posts.getTotalPages()).isEqualTo(2);
        assertThat(posts.getContent()).allMatch(post -> post.getAuthor().getUsername().equals("testuser"));
    }

    @Test
    @DisplayName("사용자의 댓글 목록을 페이징으로 조회할 수 있다")
    void getUserComments_WithPaging() {
        // given
        Pageable pageable = PageRequest.of(0, 5);

        // when
        Page<Comment> comments = userProfileService.getUserComments("testuser", pageable);

        // then
        assertThat(comments).isNotNull();
        assertThat(comments.getContent()).hasSize(3);
        assertThat(comments.getTotalElements()).isEqualTo(3);
        assertThat(comments.getContent()).allMatch(comment -> comment.getAuthor().getUsername().equals("testuser"));
    }

    @Test
    @DisplayName("삭제된 게시글은 조회되지 않는다")
    void getUserPosts_ExcludeDeleted() {
        // given
        Post post = postRepository.findByAuthorUsernameAndDeletedFalseOrderByCreatedAtDesc("testuser", PageRequest.of(0, 1))
                .getContent().get(0);
        post.delete();
        postRepository.save(post);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Post> posts = userProfileService.getUserPosts("testuser", pageable);

        // then
        assertThat(posts.getTotalElements()).isEqualTo(4); // 삭제된 것 제외
        assertThat(posts.getContent()).noneMatch(Post::getDeleted);
    }

    @Test
    @DisplayName("삭제된 댓글은 조회되지 않는다")
    void getUserComments_ExcludeDeleted() {
        // given
        Comment comment = commentRepository.findByAuthorUsernameAndDeletedFalseOrderByCreatedAtDesc("testuser", PageRequest.of(0, 1))
                .getContent().get(0);
        comment.delete();
        commentRepository.save(comment);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Comment> comments = userProfileService.getUserComments("testuser", pageable);

        // then
        assertThat(comments.getTotalElements()).isEqualTo(2); // 삭제된 것 제외
        assertThat(comments.getContent()).noneMatch(Comment::getDeleted);
    }
}
