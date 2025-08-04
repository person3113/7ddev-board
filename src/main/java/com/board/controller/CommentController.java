package com.board.controller;

import com.board.domain.entity.Comment;
import com.board.domain.entity.User;
import com.board.domain.repository.UserRepository;
import com.board.service.CommentService;
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
    private final UserRepository userRepository;

    /**
     * 댓글 생성 (AJAX)
     */
    @PostMapping("/posts/{postId}/comments")
    @ResponseBody
    public ResponseEntity<Comment> createComment(
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
            return ResponseEntity.status(201).body(comment); // 201 Created
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 대댓글 생성 (AJAX)
     */
    @PostMapping("/comments/{commentId}/replies")
    @ResponseBody
    public ResponseEntity<Comment> createReply(
            @PathVariable Long commentId,
            @RequestBody Map<String, Object> request) {

        try {
            String content = (String) request.get("content");
            Long authorId = Long.valueOf(request.get("authorId").toString());

            User author = userRepository.findById(authorId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Comment reply = commentService.createReply(commentId, content, author);
            return ResponseEntity.status(201).body(reply); // 201 Created
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 게시글별 댓글 목록 조회 (AJAX)
     */
    @GetMapping("/posts/{postId}/comments")
    @ResponseBody
    public ResponseEntity<Page<Comment>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = commentService.findCommentsByPost(postId, pageable);
        return ResponseEntity.ok(comments);
    }

    /**
     * 댓글 단건 조회 (AJAX)
     */
    @GetMapping("/comments/{id}")
    @ResponseBody
    public ResponseEntity<Comment> getComment(@PathVariable Long id) {
        try {
            Comment comment = commentService.findById(id);
            return ResponseEntity.ok(comment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 댓글 수정 (AJAX)
     */
    @PutMapping("/comments/{id}")
    @ResponseBody
    public ResponseEntity<Comment> updateComment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        try {
            String content = (String) request.get("content");
            Long authorId = Long.valueOf(request.get("authorId").toString());

            User author = userRepository.findById(authorId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Comment updatedComment = commentService.updateComment(id, content, author);
            return ResponseEntity.ok(updatedComment);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseEntity.status(403).build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 댓글 삭제 (AJAX)
     */
    @DeleteMapping("/comments/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        try {
            Long authorId = Long.valueOf(request.get("authorId").toString());

            User author = userRepository.findById(authorId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            commentService.deleteComment(id, author);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseEntity.status(403).build();
            }
            return ResponseEntity.badRequest().build();
        }
    }
}
