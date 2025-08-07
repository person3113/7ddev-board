package com.board.domain.repository;

import com.board.domain.entity.ReportedComment;
import com.board.domain.entity.Comment;
import com.board.domain.entity.User;
import com.board.domain.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReportedCommentRepository extends JpaRepository<ReportedComment, Long> {

    /**
     * 특정 댓글에 대한 신고 목록 조회
     */
    List<ReportedComment> findByComment(Comment comment);

    /**
     * 특정 신고자의 신고 목록 조회
     */
    List<ReportedComment> findByReporter(User reporter);

    /**
     * 상태별 신고 목록 조회 (페이징)
     */
    Page<ReportedComment> findByStatus(ReportStatus status, Pageable pageable);

    /**
     * 특정 댓글과 신고자로 신고 내역 조회 (중복 신고 방지용)
     */
    Optional<ReportedComment> findByCommentAndReporter(Comment comment, User reporter);

    /**
     * 대기중인 신고 개수 조회
     */
    @Query("SELECT COUNT(rc) FROM ReportedComment rc WHERE rc.status = :status")
    long countByStatus(@Param("status") ReportStatus status);

    /**
     * 최근 신고 목록 조회
     */
    @Query("SELECT rc FROM ReportedComment rc ORDER BY rc.createdAt DESC")
    Page<ReportedComment> findRecentReports(Pageable pageable);
}
