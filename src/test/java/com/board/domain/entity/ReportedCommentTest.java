package com.board.domain.entity;

import com.board.domain.enums.ReportStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class ReportedCommentTest {

    @Test
    void 신고된_댓글_생성_성공() {
        // given
        User reporter = User.builder()
                .username("reporter")
                .email("reporter@test.com")
                .password("password")
                .nickname("신고자")
                .build();

        User author = User.builder()
                .username("author")
                .email("author@test.com")
                .password("password")
                .nickname("작성자")
                .build();

        User commentAuthor = User.builder()
                .username("commentAuthor")
                .email("commentAuthor@test.com")
                .password("password")
                .nickname("댓글작성자")
                .build();

        Post post = Post.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .author(author)
                .build();

        Comment comment = Comment.builder()
                .content("테스트 댓글")
                .post(post)
                .author(commentAuthor)
                .build();

        // when
        ReportedComment reportedComment = ReportedComment.builder()
                .comment(comment)
                .reporter(reporter)
                .reason("부적절한 댓글")
                .status(ReportStatus.PENDING)
                .build();

        // then
        assertThat(reportedComment.getComment()).isEqualTo(comment);
        assertThat(reportedComment.getReporter()).isEqualTo(reporter);
        assertThat(reportedComment.getReason()).isEqualTo("부적절한 댓글");
        assertThat(reportedComment.getStatus()).isEqualTo(ReportStatus.PENDING);
    }

    @Test
    void 신고_상태_변경_가능() {
        // given
        User reporter = User.builder()
                .username("reporter")
                .email("reporter@test.com")
                .password("password")
                .nickname("신고자")
                .build();

        User author = User.builder()
                .username("author")
                .email("author@test.com")
                .password("password")
                .nickname("작성자")
                .build();

        User commentAuthor = User.builder()
                .username("commentAuthor")
                .email("commentAuthor@test.com")
                .password("password")
                .nickname("댓글작성자")
                .build();

        Post post = Post.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .author(author)
                .build();

        Comment comment = Comment.builder()
                .content("테스트 댓글")
                .post(post)
                .author(commentAuthor)
                .build();

        ReportedComment reportedComment = ReportedComment.builder()
                .comment(comment)
                .reporter(reporter)
                .reason("부적절한 댓글")
                .status(ReportStatus.PENDING)
                .build();

        // when
        reportedComment.updateStatus(ReportStatus.DISMISSED);

        // then
        assertThat(reportedComment.getStatus()).isEqualTo(ReportStatus.DISMISSED);
    }

    @Test
    void 신고_사유_검증() {
        // given
        String validReason = "부적절한 댓글";
        String longReason = "a".repeat(501);

        // when & then
        assertThatCode(() -> {
            ReportedComment.builder()
                    .reason(validReason)
                    .build();
        }).doesNotThrowAnyException();

        assertThatThrownBy(() -> {
            ReportedComment.builder()
                    .reason(longReason)
                    .build();
        }).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("신고 사유는 500자를 초과할 수 없습니다");
    }

    @Test
    void 기본_상태는_PENDING() {
        // given
        User reporter = User.builder()
                .username("reporter")
                .email("reporter@test.com")
                .password("password")
                .nickname("신고자")
                .build();

        User author = User.builder()
                .username("author")
                .email("author@test.com")
                .password("password")
                .nickname("작성자")
                .build();

        User commentAuthor = User.builder()
                .username("commentAuthor")
                .email("commentAuthor@test.com")
                .password("password")
                .nickname("댓글작성자")
                .build();

        Post post = Post.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .author(author)
                .build();

        Comment comment = Comment.builder()
                .content("테스트 댓글")
                .post(post)
                .author(commentAuthor)
                .build();

        // when - status를 명시하지 않음
        ReportedComment reportedComment = ReportedComment.builder()
                .comment(comment)
                .reporter(reporter)
                .reason("부적절한 댓글")
                .build();

        // then
        assertThat(reportedComment.getStatus()).isEqualTo(ReportStatus.PENDING);
    }
}
