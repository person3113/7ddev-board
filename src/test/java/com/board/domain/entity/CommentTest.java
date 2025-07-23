package com.board.domain.entity;

import com.board.domain.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Comment 엔티티 테스트")
class CommentTest {

    private User author;
    private Post post;

    @BeforeEach
    void setUp() {
        author = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .nickname("테스트유저")
                .role(Role.USER)
                .build();

        post = Post.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .category("자유게시판")
                .author(author)
                .build();
    }

    @Test
    @DisplayName("댓글을 정상적으로 생성할 수 있다")
    void createComment() {
        // given
        String content = "테스트 댓글입니다.";

        // when
        Comment comment = Comment.builder()
                .content(content)
                .post(post)
                .author(author)
                .build();

        // then
        assertThat(comment.getContent()).isEqualTo(content);
        assertThat(comment.getPost()).isEqualTo(post);
        assertThat(comment.getAuthor()).isEqualTo(author);
        assertThat(comment.getParent()).isNull();
        assertThat(comment.getLikeCount()).isEqualTo(0);
        assertThat(comment.isDeleted()).isFalse();
        assertThat(comment.getCreatedAt()).isNotNull();
        assertThat(comment.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("대댓글을 생성할 수 있다")
    void createReplyComment() {
        // given
        Comment parentComment = Comment.builder()
                .content("원댓글입니다.")
                .post(post)
                .author(author)
                .build();

        String replyContent = "대댓글입니다.";

        // when
        Comment replyComment = Comment.builder()
                .content(replyContent)
                .post(post)
                .author(author)
                .parent(parentComment)
                .build();

        // then
        assertThat(replyComment.getContent()).isEqualTo(replyContent);
        assertThat(replyComment.getParent()).isEqualTo(parentComment);
        assertThat(replyComment.getPost()).isEqualTo(post);
    }

    @Test
    @DisplayName("내용이 null이면 예외가 발생한다")
    void createCommentWithNullContent() {
        // when & then
        assertThatThrownBy(() -> Comment.builder()
                .content(null)
                .post(post)
                .author(author)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("댓글 내용은 필수입니다");
    }

    @Test
    @DisplayName("게시글이 null이면 예외가 발생한다")
    void createCommentWithNullPost() {
        // when & then
        assertThatThrownBy(() -> Comment.builder()
                .content("테스트 댓글")
                .post(null)
                .author(author)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("게시글은 필수입니다");
    }

    @Test
    @DisplayName("작성자가 null이면 예외가 발생한다")
    void createCommentWithNullAuthor() {
        // when & then
        assertThatThrownBy(() -> Comment.builder()
                .content("테스트 댓글")
                .post(post)
                .author(null)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("작성자는 필수입니다");
    }

    @Test
    @DisplayName("댓글 내용을 수정할 수 있다")
    void updateComment() {
        // given
        Comment comment = Comment.builder()
                .content("원본 댓글")
                .post(post)
                .author(author)
                .build();

        String newContent = "수정된 댓글";

        // when
        comment.updateContent(newContent);

        // then
        assertThat(comment.getContent()).isEqualTo(newContent);
    }

    @Test
    @DisplayName("좋아요 수를 증가시킬 수 있다")
    void increaseLikeCount() {
        // given
        Comment comment = Comment.builder()
                .content("테스트 댓글")
                .post(post)
                .author(author)
                .build();

        // when
        comment.increaseLikeCount();

        // then
        assertThat(comment.getLikeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("댓글을 소프트 삭제할 수 있다")
    void deleteComment() {
        // given
        Comment comment = Comment.builder()
                .content("테스트 댓글")
                .post(post)
                .author(author)
                .build();

        // when
        comment.delete();

        // then
        assertThat(comment.isDeleted()).isTrue();
        assertThat(comment.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("작성자 권한을 확인할 수 있다")
    void checkAuthorPermission() {
        // given
        Comment comment = Comment.builder()
                .content("테스트 댓글")
                .post(post)
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
        assertThat(comment.isAuthor(author)).isTrue();
        assertThat(comment.isAuthor(otherUser)).isFalse();
    }
}
