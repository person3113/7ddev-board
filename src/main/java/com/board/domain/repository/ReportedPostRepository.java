package com.board.domain.repository;

import com.board.domain.entity.ReportedPost;
import com.board.domain.entity.Post;
import com.board.domain.entity.User;
import com.board.domain.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReportedPostRepository extends JpaRepository<ReportedPost, Long> {

    /**
     * 특정 게시글에 대한 신고 목록 조회
     */
    List<ReportedPost> findByPost(Post post);

    /**
     * 특정 신고자의 신고 목록 조회
     */
    List<ReportedPost> findByReporter(User reporter);

    /**
     * 상태별 신고 목록 조회 (페이징)
     */
    Page<ReportedPost> findByStatus(ReportStatus status, Pageable pageable);

    /**
     * 특정 게시글과 신고자로 신고 내역 조회 (중복 신고 방지용)
     */
    Optional<ReportedPost> findByPostAndReporter(Post post, User reporter);

    /**
     * 대기중인 신고 개수 조회
     */
    @Query("SELECT COUNT(rp) FROM ReportedPost rp WHERE rp.status = :status")
    long countByStatus(@Param("status") ReportStatus status);

    /**
     * 최근 신고 목록 조회
     */
    @Query("SELECT rp FROM ReportedPost rp ORDER BY rp.createdAt DESC")
    Page<ReportedPost> findRecentReports(Pageable pageable);
}
