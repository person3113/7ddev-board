package com.board.domain.entity;

import com.board.domain.enums.ReportStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class ReportedPostTest {

    @Test
    void 신고된_게시글_생성_성공() {
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

        Post post = Post.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .author(author)
                .build();

        // when
        ReportedPost reportedPost = ReportedPost.builder()
                .post(post)
                .reporter(reporter)
                .reason("부적절한 내용")
                .status(ReportStatus.PENDING)
                .build();

        // then
        assertThat(reportedPost.getPost()).isEqualTo(post);
        assertThat(reportedPost.getReporter()).isEqualTo(reporter);
        assertThat(reportedPost.getReason()).isEqualTo("부적절한 내용");
        assertThat(reportedPost.getStatus()).isEqualTo(ReportStatus.PENDING);
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

        Post post = Post.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .author(author)
                .build();

        ReportedPost reportedPost = ReportedPost.builder()
                .post(post)
                .reporter(reporter)
                .reason("부적절한 내용")
                .status(ReportStatus.PENDING)
                .build();

        // when
        reportedPost.updateStatus(ReportStatus.RESOLVED);

        // then
        assertThat(reportedPost.getStatus()).isEqualTo(ReportStatus.RESOLVED);
    }

    @Test
    void 신고_사유_검증() {
        // given
        String validReason = "부적절한 내용";
        String longReason = "a".repeat(501);

        // when & then
        assertThatCode(() -> {
            ReportedPost.builder()
                    .reason(validReason)
                    .build();
        }).doesNotThrowAnyException();

        assertThatThrownBy(() -> {
            ReportedPost.builder()
                    .reason(longReason)
                    .build();
        }).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("신고 사유는 500자를 초과할 수 없습니다");
    }
}
