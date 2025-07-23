package com.board.domain.entity;

import com.board.domain.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Post 엔티티 테스트")
class PostTest {

    private User author;

    @BeforeEach
    void setUp() {
        author = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .nickname("테스트유저")
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("게시글을 정상적으로 생성할 수 있다")
    void createPost() {
        // given
        String title = "테스트 게시글";
        String content = "테스트 내용입니다.";
        String category = "자유게시판";

        // when
        Post post = Post.builder()
                .title(title)
                .content(content)
                .category(category)
                .author(author)
                .build();

        // then
        assertThat(post.getTitle()).isEqualTo(title);
        assertThat(post.getContent()).isEqualTo(content);
        assertThat(post.getCategory()).isEqualTo(category);
        assertThat(post.getAuthor()).isEqualTo(author);
        assertThat(post.getViewCount()).isEqualTo(0);
        assertThat(post.getLikeCount()).isEqualTo(0);
        assertThat(post.isDeleted()).isFalse();
        assertThat(post.getCreatedAt()).isNotNull();
        assertThat(post.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("제목이 null이면 예외가 발생한다")
    void createPostWithNullTitle() {
        // when & then
        assertThatThrownBy(() -> Post.builder()
                .title(null)
                .content("테스트 내용")
                .category("자유게시판")
                .author(author)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("제목은 필수입니다");
    }

    @Test
    @DisplayName("내용이 null이면 예외가 발생한다")
    void createPostWithNullContent() {
        // when & then
        assertThatThrownBy(() -> Post.builder()
                .title("테스트 제목")
                .content(null)
                .category("자유게시판")
                .author(author)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("내용은 필수입니다");
    }

    @Test
    @DisplayName("작성자가 null이면 예외가 발생한다")
    void createPostWithNullAuthor() {
        // when & then
        assertThatThrownBy(() -> Post.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .category("자유게시판")
                .author(null)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("작성자는 필수입니다");
    }

    @Test
    @DisplayName("게시글 내용을 수정할 수 있다")
    void updatePost() {
        // given
        Post post = Post.builder()
                .title("원본 제목")
                .content("원본 내용")
                .category("자유게시판")
                .author(author)
                .build();

        String newTitle = "수정된 제목";
        String newContent = "수정된 내용";
        String newCategory = "공지사항";

        // when
        post.update(newTitle, newContent, newCategory);

        // then
        assertThat(post.getTitle()).isEqualTo(newTitle);
        assertThat(post.getContent()).isEqualTo(newContent);
        assertThat(post.getCategory()).isEqualTo(newCategory);
    }

    @Test
    @DisplayName("조회수를 증가시킬 수 있다")
    void increaseViewCount() {
        // given
        Post post = Post.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .category("자유게시판")
                .author(author)
                .build();

        // when
        post.increaseViewCount();

        // then
        assertThat(post.getViewCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("좋아요 수를 증가시킬 수 있다")
    void increaseLikeCount() {
        // given
        Post post = Post.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .category("자유게시판")
                .author(author)
                .build();

        // when
        post.increaseLikeCount();

        // then
        assertThat(post.getLikeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("게시글을 소프트 삭제할 수 있다")
    void deletePost() {
        // given
        Post post = Post.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .category("자유게시판")
                .author(author)
                .build();

        // when
        post.delete();

        // then
        assertThat(post.isDeleted()).isTrue();
        assertThat(post.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("작성자 권한을 확인할 수 있다")
    void checkAuthorPermission() {
        // given
        Post post = Post.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .category("자유게시판")
                .author(author)
                .build();

        User otherUser = User.builder()
                .username("otheruser")
                .email("other@example.com")
                .password("password123")
                .nickname("다른유저")
                .role(Role.USER)
                .build();

        // when & then
        assertThat(post.isAuthor(author)).isTrue();
        assertThat(post.isAuthor(otherUser)).isFalse();
    }
}
