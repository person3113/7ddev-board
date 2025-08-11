package com.board.controller;

import com.board.domain.entity.ReportedComment;
import com.board.domain.entity.ReportedPost;
import com.board.domain.entity.User;
import com.board.domain.enums.ReportStatus;
import com.board.service.ReportService;
import com.board.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;
    private final UserService userService;

    /**
     * 게시글 신고 처리 (AJAX)
     */
    @PostMapping("/posts/{postId}/report")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reportPost(
            @PathVariable Long postId,
            @RequestParam String reason) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Spring Security에서 현재 인증된 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getPrincipal())) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            String username = authentication.getName();
            User user = userService.findByUsername(username);

            ReportedPost reportedPost = reportService.reportPost(postId, user.getId(), reason);

            response.put("success", true);
            response.put("message", "신고가 접수되었습니다.");
            response.put("reportId", reportedPost.getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("게시글 신고 처리 중 오류 발생", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 댓글 신고 처리 (AJAX)
     */
    @PostMapping("/api/comments/{commentId}/report")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reportComment(
            @PathVariable Long commentId,
            @RequestParam String reason) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Spring Security에서 현재 인증된 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getPrincipal())) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            String username = authentication.getName();
            User user = userService.findByUsername(username);

            ReportedComment reportedComment = reportService.reportComment(commentId, user.getId(), reason);

            response.put("success", true);
            response.put("message", "신고가 접수되었습니다.");
            response.put("reportId", reportedComment.getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("댓글 신고 처리 중 오류 발생", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 관리자 - 신고 관리 페이지
     */
    @GetMapping("/admin/reports")
    public String adminReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "post") String type,
            @RequestParam(defaultValue = "PENDING") String status,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        ReportStatus reportStatus = ReportStatus.valueOf(status);

        if ("post".equals(type)) {
            Page<ReportedPost> reports = reportService.getPostReportsByStatus(reportStatus, pageable);
            model.addAttribute("reports", reports);
            model.addAttribute("reportType", "post");
        } else {
            Page<ReportedComment> reports = reportService.getCommentReportsByStatus(reportStatus, pageable);
            model.addAttribute("reports", reports);
            model.addAttribute("reportType", "comment");
        }

        // 신고 통계
        ReportService.ReportStats stats = reportService.getReportStats();
        model.addAttribute("stats", stats);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentType", type);

        return "admin/reports";
    }

    /**
     * 관리자 - 게시글 신고 상태 변경
     */
    @PostMapping("/admin/reports/posts/{reportId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updatePostReportStatus(
            @PathVariable Long reportId,
            @RequestParam String status) {

        Map<String, Object> response = new HashMap<>();

        try {
            ReportStatus reportStatus = ReportStatus.valueOf(status);
            reportService.updatePostReportStatus(reportId, reportStatus);

            response.put("success", true);
            response.put("message", "신고 상태가 변경되었습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("신고 상태 변경 중 오류 발생", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 관리자 - 댓글 신고 상태 변경
     */
    @PostMapping("/admin/reports/comments/{reportId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCommentReportStatus(
            @PathVariable Long reportId,
            @RequestParam String status) {

        Map<String, Object> response = new HashMap<>();

        try {
            ReportStatus reportStatus = ReportStatus.valueOf(status);
            reportService.updateCommentReportStatus(reportId, reportStatus);

            response.put("success", true);
            response.put("message", "신고 상태가 변경되었습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("신고 상태 변경 중 오류 발생", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
