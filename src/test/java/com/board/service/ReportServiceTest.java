package com.board.service;

import com.board.domain.entity.*;
import com.board.domain.enums.ReportStatus;
import com.board.domain.repository.*;
import com.board.exception.AlreadyReportedException;
import com.board.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportedPostRepository reportedPostRepository;

    @Mock
    private ReportedCommentRepository reportedCommentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReportService reportService;

    private User reporter;
    private User author;
    private Post post;
    private Comment comment;

    @BeforeEach
    void setUp() {
        reporter = User.builder()
                .username("reporter")
                .email("reporter@test.com")
                .password("password")
                .nickname("신고자")
                .build();

        author = User.builder()
                .username("author")
                .email("author@test.com")
                .password("password")
                .nickname("작성자")
                .build();

        post = Post.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .author(author)
                .build();

        comment = Comment.builder()
                .content("테스트 댓글")
                .post(post)
                .author(author)
                .build();
    }

    @Test
    void 게시글_신고_성공() {
        // given
        Long postId = 1L;
        Long reporterId = 2L;
        String reason = "부적절한 내용";

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userRepository.findById(reporterId)).thenReturn(Optional.of(reporter));
        when(reportedPostRepository.findByPostAndReporter(post, reporter)).thenReturn(Optional.empty());

        ReportedPost savedReport = ReportedPost.builder()
                .post(post)
                .reporter(reporter)
                .reason(reason)
                .status(ReportStatus.PENDING)
                .build();
        when(reportedPostRepository.save(any(ReportedPost.class))).thenReturn(savedReport);

        // when
        ReportedPost result = reportService.reportPost(postId, reporterId, reason);

        // then
        assertThat(result.getPost()).isEqualTo(post);
        assertThat(result.getReporter()).isEqualTo(reporter);
        assertThat(result.getReason()).isEqualTo(reason);
        assertThat(result.getStatus()).isEqualTo(ReportStatus.PENDING);

        verify(reportedPostRepository).save(any(ReportedPost.class));
    }

    @Test
    void 게시글_중복_신고_방지() {
        // given
        Long postId = 1L;
        Long reporterId = 2L;
        String reason = "부적절한 내용";

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userRepository.findById(reporterId)).thenReturn(Optional.of(reporter));

        ReportedPost existingReport = ReportedPost.builder()
                .post(post)
                .reporter(reporter)
                .reason("이전 신고")
                .status(ReportStatus.PENDING)
                .build();
        when(reportedPostRepository.findByPostAndReporter(post, reporter)).thenReturn(Optional.of(existingReport));

        // when & then
        assertThatThrownBy(() -> reportService.reportPost(postId, reporterId, reason))
                .isInstanceOf(AlreadyReportedException.class)
                .hasMessageContaining("이미 신고한 게시글입니다");

        verify(reportedPostRepository, never()).save(any(ReportedPost.class));
    }

    @Test
    void 댓글_신고_성공() {
        // given
        Long commentId = 1L;
        Long reporterId = 2L;
        String reason = "부적절한 댓글";

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(userRepository.findById(reporterId)).thenReturn(Optional.of(reporter));
        when(reportedCommentRepository.findByCommentAndReporter(comment, reporter)).thenReturn(Optional.empty());

        ReportedComment savedReport = ReportedComment.builder()
                .comment(comment)
                .reporter(reporter)
                .reason(reason)
                .status(ReportStatus.PENDING)
                .build();
        when(reportedCommentRepository.save(any(ReportedComment.class))).thenReturn(savedReport);

        // when
        ReportedComment result = reportService.reportComment(commentId, reporterId, reason);

        // then
        assertThat(result.getComment()).isEqualTo(comment);
        assertThat(result.getReporter()).isEqualTo(reporter);
        assertThat(result.getReason()).isEqualTo(reason);
        assertThat(result.getStatus()).isEqualTo(ReportStatus.PENDING);

        verify(reportedCommentRepository).save(any(ReportedComment.class));
    }

    @Test
    void 신고_상태_변경_성공() {
        // given
        Long reportId = 1L;
        ReportedPost reportedPost = ReportedPost.builder()
                .post(post)
                .reporter(reporter)
                .reason("부적절한 내용")
                .status(ReportStatus.PENDING)
                .build();

        when(reportedPostRepository.findById(reportId)).thenReturn(Optional.of(reportedPost));
        when(reportedPostRepository.save(any(ReportedPost.class))).thenReturn(reportedPost);

        // when
        reportService.updatePostReportStatus(reportId, ReportStatus.RESOLVED);

        // then
        verify(reportedPostRepository).save(reportedPost);
        assertThat(reportedPost.getStatus()).isEqualTo(ReportStatus.RESOLVED);
    }

    @Test
    void 대기중인_신고_목록_조회() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        ReportedPost report1 = ReportedPost.builder()
                .post(post)
                .reporter(reporter)
                .reason("신고1")
                .status(ReportStatus.PENDING)
                .build();

        ReportedPost report2 = ReportedPost.builder()
                .post(post)
                .reporter(reporter)
                .reason("신고2")
                .status(ReportStatus.PENDING)
                .build();

        Page<ReportedPost> reportPage = new PageImpl<>(Arrays.asList(report1, report2));
        when(reportedPostRepository.findByStatus(ReportStatus.PENDING, pageable)).thenReturn(reportPage);

        // when
        Page<ReportedPost> result = reportService.getPendingPostReports(pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).containsExactly(report1, report2);
    }

    @Test
    void 존재하지_않는_게시글_신고시_예외발생() {
        // given
        Long nonExistentPostId = 999L;
        Long reporterId = 2L;
        String reason = "부적절한 내용";

        when(postRepository.findById(nonExistentPostId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reportService.reportPost(nonExistentPostId, reporterId, reason))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다");
    }
}
