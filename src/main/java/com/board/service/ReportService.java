package com.board.service;

import com.board.domain.entity.*;
import com.board.domain.enums.ReportStatus;
import com.board.domain.repository.*;
import com.board.exception.AlreadyReportedException;
import com.board.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReportService {

    private final ReportedPostRepository reportedPostRepository;
    private final ReportedCommentRepository reportedCommentRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    /**
     * 게시글 신고
     */
    @Transactional
    public ReportedPost reportPost(Long postId, Long reporterId, String reason) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다. ID: " + postId));

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다. ID: " + reporterId));

        // 중복 신고 방지
        if (reportedPostRepository.findByPostAndReporter(post, reporter).isPresent()) {
            throw new AlreadyReportedException("이미 신고한 게시글입니다.");
        }

        ReportedPost reportedPost = ReportedPost.builder()
                .post(post)
                .reporter(reporter)
                .reason(reason)
                .status(ReportStatus.PENDING)
                .build();

        ReportedPost saved = reportedPostRepository.save(reportedPost);
        log.info("게시글 신고 완료 - Post ID: {}, Reporter ID: {}, Report ID: {}",
                postId, reporterId, saved.getId());

        return saved;
    }

    /**
     * 댓글 신고
     */
    @Transactional
    public ReportedComment reportComment(Long commentId, Long reporterId, String reason) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("댓글을 찾을 수 없습니다. ID: " + commentId));

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다. ID: " + reporterId));

        // 중복 신고 방지
        if (reportedCommentRepository.findByCommentAndReporter(comment, reporter).isPresent()) {
            throw new AlreadyReportedException("이미 신고한 댓글입니다.");
        }

        ReportedComment reportedComment = ReportedComment.builder()
                .comment(comment)
                .reporter(reporter)
                .reason(reason)
                .status(ReportStatus.PENDING)
                .build();

        ReportedComment saved = reportedCommentRepository.save(reportedComment);
        log.info("댓글 신고 완료 - Comment ID: {}, Reporter ID: {}, Report ID: {}",
                commentId, reporterId, saved.getId());

        return saved;
    }

    /**
     * 댓글 신고 (User 엔티티 사용)
     */
    @Transactional
    public ReportedComment reportComment(Long commentId, User reporter, String reason) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("댓글을 찾을 수 없습니다. ID: " + commentId));

        // 중복 신고 방지
        if (reportedCommentRepository.findByCommentAndReporter(comment, reporter).isPresent()) {
            throw new AlreadyReportedException("이미 신고한 댓글입니다.");
        }

        ReportedComment reportedComment = ReportedComment.builder()
                .comment(comment)
                .reporter(reporter)
                .reason(reason)
                .status(ReportStatus.PENDING)
                .build();

        ReportedComment saved = reportedCommentRepository.save(reportedComment);
        log.info("댓글 신고 완료 - Comment ID: {}, Reporter ID: {}, Report ID: {}",
                commentId, reporter.getId(), saved.getId());

        return saved;
    }

    /**
     * 게시글 신고 상태 변경
     */
    @Transactional
    public void updatePostReportStatus(Long reportId, ReportStatus status) {
        ReportedPost reportedPost = reportedPostRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("신고를 찾을 수 없습니다. ID: " + reportId));

        reportedPost.updateStatus(status);
        reportedPostRepository.save(reportedPost);

        log.info("게시글 신고 상태 변경 - Report ID: {}, Status: {}", reportId, status);
    }

    /**
     * 댓글 신고 상태 변경
     */
    @Transactional
    public void updateCommentReportStatus(Long reportId, ReportStatus status) {
        ReportedComment reportedComment = reportedCommentRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("신고를 찾을 수 없습니다. ID: " + reportId));

        reportedComment.updateStatus(status);
        reportedCommentRepository.save(reportedComment);

        log.info("댓글 신고 상태 변경 - Report ID: {}, Status: {}", reportId, status);
    }

    /**
     * 대기중인 게시글 신고 목록 조회
     */
    public Page<ReportedPost> getPendingPostReports(Pageable pageable) {
        return reportedPostRepository.findByStatus(ReportStatus.PENDING, pageable);
    }

    /**
     * 대기중인 댓글 신고 목록 조회
     */
    public Page<ReportedComment> getPendingCommentReports(Pageable pageable) {
        return reportedCommentRepository.findByStatus(ReportStatus.PENDING, pageable);
    }

    /**
     * 상태별 게시글 신고 목록 조회
     */
    public Page<ReportedPost> getPostReportsByStatus(ReportStatus status, Pageable pageable) {
        return reportedPostRepository.findByStatus(status, pageable);
    }

    /**
     * 상태별 댓글 신고 목록 조회
     */
    public Page<ReportedComment> getCommentReportsByStatus(ReportStatus status, Pageable pageable) {
        return reportedCommentRepository.findByStatus(status, pageable);
    }

    /**
     * 최근 신고 목록 조회
     */
    public Page<ReportedPost> getRecentPostReports(Pageable pageable) {
        return reportedPostRepository.findRecentReports(pageable);
    }

    /**
     * 최근 댓글 신고 목록 조회
     */
    public Page<ReportedComment> getRecentCommentReports(Pageable pageable) {
        return reportedCommentRepository.findRecentReports(pageable);
    }

    /**
     * 특정 게시글의 신고 목록 조회
     */
    public List<ReportedPost> getPostReports(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다. ID: " + postId));

        return reportedPostRepository.findByPost(post);
    }

    /**
     * 특정 댓글의 신고 목록 조회
     */
    public List<ReportedComment> getCommentReports(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("댓글을 찾을 수 없습니다. ID: " + commentId));

        return reportedCommentRepository.findByComment(comment);
    }

    /**
     * 신고 통계 조회
     */
    public ReportStats getReportStats() {
        long pendingPostReports = reportedPostRepository.countByStatus(ReportStatus.PENDING);
        long pendingCommentReports = reportedCommentRepository.countByStatus(ReportStatus.PENDING);
        long resolvedPostReports = reportedPostRepository.countByStatus(ReportStatus.RESOLVED);
        long resolvedCommentReports = reportedCommentRepository.countByStatus(ReportStatus.RESOLVED);

        return ReportStats.builder()
                .pendingPostReports(pendingPostReports)
                .pendingCommentReports(pendingCommentReports)
                .resolvedPostReports(resolvedPostReports)
                .resolvedCommentReports(resolvedCommentReports)
                .build();
    }

    /**
     * 신고 통계 DTO
     */
    public static class ReportStats {
        private final long pendingPostReports;
        private final long pendingCommentReports;
        private final long resolvedPostReports;
        private final long resolvedCommentReports;

        public ReportStats(long pendingPostReports, long pendingCommentReports,
                          long resolvedPostReports, long resolvedCommentReports) {
            this.pendingPostReports = pendingPostReports;
            this.pendingCommentReports = pendingCommentReports;
            this.resolvedPostReports = resolvedPostReports;
            this.resolvedCommentReports = resolvedCommentReports;
        }

        public static ReportStatsBuilder builder() {
            return new ReportStatsBuilder();
        }

        public long getPendingPostReports() { return pendingPostReports; }
        public long getPendingCommentReports() { return pendingCommentReports; }
        public long getResolvedPostReports() { return resolvedPostReports; }
        public long getResolvedCommentReports() { return resolvedCommentReports; }
        public long getTotalPendingReports() { return pendingPostReports + pendingCommentReports; }
        public long getTotalResolvedReports() { return resolvedPostReports + resolvedCommentReports; }

        public static class ReportStatsBuilder {
            private long pendingPostReports;
            private long pendingCommentReports;
            private long resolvedPostReports;
            private long resolvedCommentReports;

            public ReportStatsBuilder pendingPostReports(long pendingPostReports) {
                this.pendingPostReports = pendingPostReports;
                return this;
            }

            public ReportStatsBuilder pendingCommentReports(long pendingCommentReports) {
                this.pendingCommentReports = pendingCommentReports;
                return this;
            }

            public ReportStatsBuilder resolvedPostReports(long resolvedPostReports) {
                this.resolvedPostReports = resolvedPostReports;
                return this;
            }

            public ReportStatsBuilder resolvedCommentReports(long resolvedCommentReports) {
                this.resolvedCommentReports = resolvedCommentReports;
                return this;
            }

            public ReportStats build() {
                return new ReportStats(pendingPostReports, pendingCommentReports,
                                     resolvedPostReports, resolvedCommentReports);
            }
        }
    }
}
