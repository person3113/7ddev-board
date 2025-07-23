package com.board.domain.repository;

import com.board.domain.entity.Comment;
import com.board.domain.entity.Post;
import com.board.domain.entity.User;
import com.board.domain.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CommentRepository 통합 테스트")
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

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
        userRepository.save(author);

        post = Post.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .category("자유게시판")
                .author(author)
                .build();
        postRepository.save(post);
    }

    @Test
    @DisplayName("특정 게시글의 댓글을 조회할 수 있다")
    void findByPost() {
        // given
        Comment comment1 = Comment.builder()
                .content("첫 번째 댓글")
                .post(post)
                .author(author)
                .build();

        Comment comment2 = Comment.builder()
                .content("두 번째 댓글")
                .post(post)
                .author(author)
                .build();

        commentRepository.save(comment1);
        commentRepository.save(comment2);

        // when
        Page<Comment> comments = commentRepository.findByPostAndDeletedFalseOrderByCreatedAtAsc(
                post, PageRequest.of(0, 10));

        // then
        assertThat(comments.getContent()).hasSize(2);
        assertThat(comments.getContent().get(0).getContent()).isEqualTo("첫 번째 댓글");
    }

    @Test
    @DisplayName("최상위 댓글만 조회할 수 있다")
    void findTopLevelComments() {
        // given
        Comment parentComment = Comment.builder()
                .content("원댓글")
                .post(post)
                .author(author)
                .build();
        commentRepository.save(parentComment);

        Comment replyComment = Comment.builder()
                .content("대댓글")
                .post(post)
                .author(author)
                .parent(parentComment)
                .build();
        commentRepository.save(replyComment);

        // when
        Page<Comment> topLevelComments = commentRepository.findByPostAndParentIsNullAndDeletedFalseOrderByCreatedAtAsc(
                post, PageRequest.of(0, 10));

        // then
        assertThat(topLevelComments.getContent()).hasSize(1);
        assertThat(topLevelComments.getContent().get(0).getContent()).isEqualTo("원댓글");
        assertThat(topLevelComments.getContent().get(0).getParent()).isNull();
    }

    @Test
    @DisplayName("대댓글을 조회할 수 있다")
    void findRepliesByParent() {
        // given
        Comment parentComment = Comment.builder()
                .content("원댓글")
                .post(post)
                .author(author)
                .build();
        commentRepository.save(parentComment);

        Comment reply1 = Comment.builder()
                .content("대댓글1")
                .post(post)
                .author(author)
                .parent(parentComment)
                .build();

        Comment reply2 = Comment.builder()
                .content("대댓글2")
                .post(post)
                .author(author)
                .parent(parentComment)
                .build();

        commentRepository.save(reply1);
        commentRepository.save(reply2);

        // when
        List<Comment> replies = commentRepository.findByParentAndDeletedFalseOrderByCreatedAtAsc(parentComment);

        // then
        assertThat(replies).hasSize(2);
        assertThat(replies).extracting(Comment::getContent)
                .containsExactly("대댓글1", "대댓글2");
    }

    @Test
    @DisplayName("특정 게시글의 댓글 수를 조회할 수 있다")
    void countByPost() {
        // given
        Comment comment1 = Comment.builder()
                .content("댓글1")
                .post(post)
                .author(author)
                .build();

        Comment comment2 = Comment.builder()
                .content("댓글2")
                .post(post)
                .author(author)
                .build();

        commentRepository.save(comment1);
        commentRepository.save(comment2);

        // 하나 삭제
        comment2.delete();
        commentRepository.save(comment2);

        // when
        long count = commentRepository.countByPostAndDeletedFalse(post);

        // then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("계층 구조 댓글을 조회할 수 있다")
    void findCommentsWithReplies() {
        // given
        Comment parentComment = Comment.builder()
                .content("원댓글")
                .post(post)
                .author(author)
                .build();
        commentRepository.save(parentComment);

        Comment reply = Comment.builder()
                .content("대댓글")
                .post(post)
                .author(author)
                .parent(parentComment)
                .build();
        commentRepository.save(reply);

        // when - 최상위 댓글만 조회
        Page<Comment> topLevelComments = commentRepository.findByPostAndParentIsNullAndDeletedFalseOrderByCreatedAtAsc(
                post, PageRequest.of(0, 10));

        // then
        assertThat(topLevelComments.getContent()).hasSize(1);
        Comment parent = topLevelComments.getContent().get(0);
        assertThat(parent.getContent()).isEqualTo("원댓글");

        // 대댓글은 별도 조회
        List<Comment> replies = commentRepository.findByParentAndDeletedFalseOrderByCreatedAtAsc(parent);
        assertThat(replies).hasSize(1);
        assertThat(replies.get(0).getContent()).isEqualTo("대댓글");
    }
}
