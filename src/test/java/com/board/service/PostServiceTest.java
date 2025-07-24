package com.board.service;

import com.board.domain.entity.Post;
import com.board.domain.entity.User;
import com.board.domain.enums.Role;
import com.board.domain.repository.PostRepository;
import com.board.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User anotherUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .nickname("테스트유저")
                .role(Role.USER)
                .build();
        testUser = userRepository.save(testUser);

        anotherUser = User.builder()
                .username("anotheruser")
                .email("another@example.com")
                .password("password456")
                .nickname("다른유저")
                .role(Role.USER)
                .build();
        anotherUser = userRepository.save(anotherUser);
    }

    @Test
    @DisplayName("유효한 데이터로 게시글 생성 성공")
    void createPost_Success() {
        // given
        String title = "테스트 게시글";
        String content = "테스트 내용";
        String category = "자유";

        // when
        Post createdPost = postService.createPost(title, content, category, testUser);

        // then
        assertThat(createdPost).isNotNull();
        assertThat(createdPost.getId()).isNotNull();
        assertThat(createdPost.getTitle()).isEqualTo(title);
        assertThat(createdPost.getContent()).isEqualTo(content);
        assertThat(createdPost.getCategory()).isEqualTo(category);
        assertThat(createdPost.getAuthor()).isEqualTo(testUser);
        assertThat(createdPost.getViewCount()).isEqualTo(0);
        assertThat(createdPost.getLikeCount()).isEqualTo(0);
        assertThat(createdPost.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("제목이 null이면 게시글 생성 실패")
    void createPost_Fail_NullTitle() {
        // given
        String title = null;
        String content = "테스트 내용";
        String category = "자유";

        // when & then
        assertThatThrownBy(() -> postService.createPost(title, content, category, testUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("제목은 필수입니다");
    }

    @Test
    @DisplayName("내용이 null이면 게시글 생성 실패")
    void createPost_Fail_NullContent() {
        // given
        String title = "테스트 게시글";
        String content = null;
        String category = "자유";

        // when & then
        assertThatThrownBy(() -> postService.createPost(title, content, category, testUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("내용은 필수입니다");
    }

    @Test
    @DisplayName("작성자가 null이면 게시글 생성 실패")
    void createPost_Fail_NullAuthor() {
        // given
        String title = "테스트 게시글";
        String content = "테스트 내용";
        String category = "자유";

        // when & then
        assertThatThrownBy(() -> postService.createPost(title, content, category, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("작성자는 필수입니다");
    }

    @Test
    @DisplayName("ID로 게시글 조회 성공")
    void findById_Success() {
        // given
        Post savedPost = Post.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .category("자유")
                .author(testUser)
                .build();
        savedPost = postRepository.save(savedPost);

        // when
        Post foundPost = postService.findById(savedPost.getId());

        // then
        assertThat(foundPost).isNotNull();
        assertThat(foundPost.getId()).isEqualTo(savedPost.getId());
        assertThat(foundPost.getTitle()).isEqualTo(savedPost.getTitle());
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 예외 발생")
    void findById_Fail_NotFound() {
        // given
        Long nonExistentId = 999L;

        // when & then
        assertThatThrownBy(() -> postService.findById(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("게시글을 찾을 수 없습니다. ID: " + nonExistentId);
    }

    @Test
    @DisplayName("삭제된 게시글 조회 시 예외 발생")
    void findById_Fail_DeletedPost() {
        // given
        Post savedPost = Post.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .category("자유")
                .author(testUser)
                .build();
        savedPost = postRepository.save(savedPost);
        savedPost.delete();
        postRepository.save(savedPost);

        final Long postId = savedPost.getId();

        // when & then
        assertThatThrownBy(() -> postService.findById(postId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("삭제된 게시글입니다. ID: " + postId);
    }

    @Test
    @DisplayName("작성자가 게시글 수정 성공")
    void updatePost_Success() {
        // given
        Post savedPost = Post.builder()
                .title("원본 제목")
                .content("원본 내용")
                .category("자유")
                .author(testUser)
                .build();
        savedPost = postRepository.save(savedPost);

        String newTitle = "수정된 제목";
        String newContent = "수정된 내용";
        String newCategory = "공지";

        // when
        Post updatedPost = postService.updatePost(savedPost.getId(), newTitle, newContent, newCategory, testUser);

        // then
        assertThat(updatedPost.getTitle()).isEqualTo(newTitle);
        assertThat(updatedPost.getContent()).isEqualTo(newContent);
        assertThat(updatedPost.getCategory()).isEqualTo(newCategory);
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 게시글 수정 시 실패")
    void updatePost_Fail_NotAuthor() {
        // given
        Post savedPost = Post.builder()
                .title("원본 제목")
                .content("원본 내용")
                .category("자유")
                .author(testUser)
                .build();
        savedPost = postRepository.save(savedPost);

        final Long postId = savedPost.getId();

        // when & then
        assertThatThrownBy(() -> postService.updatePost(postId, "수정 제목", "수정 내용", "공지", anotherUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("게시글을 수정할 권한이 없습니다");
    }

    @Test
    @DisplayName("작성자가 게시글 삭제 성공 (소프트 삭제)")
    void deletePost_Success() {
        // given
        Post savedPost = Post.builder()
                .title("삭제할 게시글")
                .content("삭제할 내용")
                .category("자유")
                .author(testUser)
                .build();
        savedPost = postRepository.save(savedPost);

        // when
        postService.deletePost(savedPost.getId(), testUser);

        // then
        Post deletedPost = postRepository.findById(savedPost.getId()).orElseThrow();
        assertThat(deletedPost.isDeleted()).isTrue();
        assertThat(deletedPost.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 게시글 삭제 시 실패")
    void deletePost_Fail_NotAuthor() {
        // given
        Post savedPost = Post.builder()
                .title("삭제할 게시글")
                .content("삭제할 내용")
                .category("자유")
                .author(testUser)
                .build();
        savedPost = postRepository.save(savedPost);

        final Long postId = savedPost.getId();

        // when & then
        assertThatThrownBy(() -> postService.deletePost(postId, anotherUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("게시글을 삭제할 권한이 없습니다");
    }

    @Test
    @DisplayName("게시글 조회수 증가 성공")
    void increaseViewCount_Success() {
        // given
        Post savedPost = Post.builder()
                .title("조회수 테스트 게시글")
                .content("조회수 테스트 내용")
                .category("자유")
                .author(testUser)
                .build();
        savedPost = postRepository.save(savedPost);

        int initialViewCount = savedPost.getViewCount();

        // when
        Post updatedPost = postService.increaseViewCount(savedPost.getId());

        // then
        assertThat(updatedPost.getViewCount()).isEqualTo(initialViewCount + 1);
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회수 증가 시 실패")
    void increaseViewCount_Fail_NotFound() {
        // given
        Long nonExistentId = 999L;

        // when & then
        assertThatThrownBy(() -> postService.increaseViewCount(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("게시글을 찾을 수 없습니다. ID: " + nonExistentId);
    }
}
