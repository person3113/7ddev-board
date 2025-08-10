package com.board.controller;

import com.board.domain.entity.Comment;
import com.board.domain.entity.User;
import com.board.domain.repository.UserRepository;
import com.board.service.CommentService;
import com.board.service.CommentLikeService;
import com.board.service.ReportService;
import com.board.dto.CommentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final CommentLikeService commentLikeService;
    private final ReportService reportService;
    private final UserRepository userRepository;

    /**
     * 댓글 생성 (폼 제출)
     */
    @PostMapping("/posts/{postId}/comments")
    public String createComment(
            @PathVariable Long postId,
            @RequestParam String content,
            @RequestParam Long authorId) {

        try {
            if (content == null || content.trim().isEmpty()) {
                return "redirect:/posts/" + postId + "?error=empty_content";
            }

            User author = userRepository.findById(authorId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            commentService.createComment(postId, content, author);
            return "redirect:/posts/" + postId; // 성공 시 게시글 상세 페이지로 리다이렉트
        } catch (IllegalArgumentException e) {
            return "redirect:/posts/" + postId + "?error=invalid_request";
        }
    }

    /**
     * 댓글 생성 (AJAX) - API용
     */
    @PostMapping("/api/posts/{postId}/comments")
    @ResponseBody
    public ResponseEntity<CommentDto> createCommentApi(
            @PathVariable Long postId,
            @RequestBody Map<String, Object> request) {

        try {
            String content = (String) request.get("content");
            Long authorId = Long.valueOf(request.get("authorId").toString());

            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            User author = userRepository.findById(authorId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Comment comment = commentService.createComment(postId, content, author);
            CommentDto commentDto = CommentDto.from(comment);
            return ResponseEntity.status(201).body(commentDto); // 201 Created
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 대댓글 생성 (AJAX)
     */
    @PostMapping("/comments/{commentId}/replies")
    @ResponseBody
    public ResponseEntity<CommentDto> createReply(
            @PathVariable Long commentId,
            @RequestBody Map<String, Object> request) {

        try {
            String content = (String) request.get("content");
            Long authorId = Long.valueOf(request.get("authorId").toString());

            User author = userRepository.findById(authorId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Comment reply = commentService.createReply(commentId, content, author);
            CommentDto replyDto = CommentDto.from(reply);
            return ResponseEntity.status(201).body(replyDto); // 201 Created
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 게시글별 댓글 목록 조회 (AJAX)
     */
    @GetMapping("/posts/{postId}/comments")
    @ResponseBody
    public ResponseEntity<Page<CommentDto>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = commentService.findCommentsByPost(postId, pageable);
        Page<CommentDto> commentDtos = comments.map(CommentDto::from);
        return ResponseEntity.ok(commentDtos);
    }

    /**
     * 댓글 단건 조회 (AJAX)
     */
    @GetMapping("/comments/{id}")
    @ResponseBody
    public ResponseEntity<CommentDto> getComment(@PathVariable Long id) {
        try {
            Comment comment = commentService.findById(id);
            CommentDto commentDto = CommentDto.from(comment);
            return ResponseEntity.ok(commentDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 댓글 수정 (AJAX)
     */
    @PutMapping("/comments/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateComment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        try {
            String content = (String) request.get("content");
            Long authorId = Long.valueOf(request.get("authorId").toString());

            User author = userRepository.findById(authorId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Comment updatedComment = commentService.updateComment(id, content, author);

            // 무한 순환 참조를 피하기 위해 간단한 응답 객체 반환
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "댓글이 수정되었습니다.",
                "commentId", updatedComment.getId(),
                "content", updatedComment.getContent()
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "message", e.getMessage()
            );

            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseEntity.status(403).body(errorResponse);
            }
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 댓글 삭제 (AJAX)
     */
    @DeleteMapping("/comments/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteComment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        try {
            Long authorId = Long.valueOf(request.get("authorId").toString());

            User author = userRepository.findById(authorId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            commentService.deleteComment(id, author);

            Map<String, Object> response = Map.of(
                "success", true,
                "message", "댓글이 삭제되었습니다."
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "message", e.getMessage()
            );

            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseEntity.status(403).body(errorResponse);
            }
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 댓글 좋아요 토글 (AJAX)
     */
    @PostMapping("/comments/{id}/like")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleCommentLike(
            @PathVariable Long id,
            @RequestParam Long userId) {

        try {
            boolean isLiked = commentLikeService.toggleLike(id, userId);
            long likeCount = commentLikeService.getLikeCount(id);

            Map<String, Object> response = Map.of(
                "success", true,
                "isLiked", isLiked,
                "likeCount", likeCount,
                "message", isLiked ? "좋아요를 눌렀습니다." : "좋아요를 취소했습니다."
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "message", e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 댓글 신고 (AJAX)
     */
    @PostMapping("/comments/{id}/report")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reportComment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        try {
            Long reporterId = Long.valueOf(request.get("reporterId").toString());
            String reason = (String) request.get("reason");

            if (reason == null || reason.trim().isEmpty()) {
                Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", "신고 사유를 입력해주세요."
                );
                return ResponseEntity.badRequest().body(errorResponse);
            }

            reportService.reportComment(id, reporterId, reason);

            Map<String, Object> response = Map.of(
                "success", true,
                "message", "댓글이 신고되었습니다."
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "message", e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
