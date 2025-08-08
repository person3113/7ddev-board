package com.board.controller;

import com.board.domain.entity.Post;
import com.board.domain.entity.User;
import com.board.domain.repository.UserRepository;
import com.board.service.PostService;
import com.board.service.LikeService;
import com.board.service.ViewService;
import com.board.service.MarkdownService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * Post MVC 컨트롤러
 *
 * 게시글의 웹 요청을 처리하고 적절한 뷰를 반환합니다.
 * RESTful URL 패턴을 따르며, Thymeleaf 템플릿을 사용합니다.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final UserRepository userRepository;
    private final LikeService likeService;
    private final ViewService viewService;
    private final MarkdownService markdownService;

    /**
     * 게시글 목록 조회
     * GET /posts
     */
    @GetMapping
    public String getPosts(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(defaultValue = "latest") String sort,
                          Model model) {
        log.debug("게시글 목록 조회 요청 - 페이지: {}, 크기: {}, 정렬: {}", page, size, sort);

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postService.findAllWithSort(pageable, sort);

        model.addAttribute("posts", posts);
        model.addAttribute("currentSort", sort);

        log.debug("게시글 목록 조회 완료 - 전체: {}, 현재 페이지: {}",
                posts.getTotalElements(), posts.getNumberOfElements());

        return "posts/list";
    }

    /**
     * 게시글 상세 조회
     * GET /posts/{id}
     */
    @GetMapping("/{id}")
    public String getPost(@PathVariable Long id, Model model, HttpServletRequest request) {
        log.debug("게시글 상세 조회 요청 - ID: {}", id);

        try {
            Post post = postService.findById(id);

            // ViewService를 사용하여 조회수 증가 (중복 조회 방지)
            viewService.increaseViewCount(id, request);

            // 조회수가 증가된 후 다시 조회
            post = postService.findById(id);

            // 마크다운 설정에 따라 조건부로 HTML 변환
            String htmlContent;
            if (Boolean.TRUE.equals(post.getIsMarkdown())) {
                // 마크다운으로 작성된 경우: 마크다운을 HTML로 변환
                htmlContent = markdownService.markdownToHtml(post.getContent());
                log.debug("마크다운 렌더링 적용 - 게시글 ID: {}", post.getId());
            } else {
                // 일반 텍스트로 작성된 경우: HTML 이스케이프 + 줄바꿈 처리
                htmlContent = markdownService.convertPlainTextToHtml(post.getContent());
                log.debug("일반 텍스트 렌더링 적용 (줄바꿈 포함) - 게시글 ID: {}", post.getId());
            }

            model.addAttribute("post", post);
            model.addAttribute("htmlContent", htmlContent);

            log.debug("게시글 상세 조회 완료 - ID: {}, 제목: {}, 조회수: {}, 마크다운: {}",
                     post.getId(), post.getTitle(), post.getViewCount(), post.getIsMarkdown());
            return "posts/detail";
        } catch (IllegalArgumentException e) {
            log.warn("게시글 조회 실패 - ID: {}, 오류: {}", id, e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * 게시글 작성 폼
     * GET /posts/new
     */
    @GetMapping("/new")
    public String getNewPostForm(Model model) {
        log.debug("게시글 작성 폼 요청");

        // PostCreateRequest 객체 생성 시 기본값 설정
        PostCreateRequest request = PostCreateRequest.builder()
                .title("")
                .content("")
                .category("")
                .authorId(1L) // 임시로 1번 사용자 고정
                .build();

        model.addAttribute("post", request);

        return "posts/form";
    }

    /**
     * 게시글 생성
     * POST /posts
     */
    @PostMapping
    public String createPost(@Valid @ModelAttribute("post") PostCreateRequest request,
                            BindingResult bindingResult,
                            Model model) {
        log.debug("게시글 생성 요청 - 제목: {}, 작성자 ID: {}, 마크다운: {}",
                 request.getTitle(), request.getAuthorId(), request.getIsMarkdown());

        if (bindingResult.hasErrors()) {
            log.warn("게시글 생성 검증 실패 - 오류: {}", bindingResult.getAllErrors());
            return "posts/form";
        }

        try {
            User author = userRepository.findById(request.getAuthorId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            // 마크다운 지원 메서드 사용
            Post post = postService.createPost(
                    request.getTitle(),
                    request.getContent(),
                    request.getCategory(),
                    author,
                    request.getIsMarkdown()
            );

            log.info("게시글 생성 완료 - ID: {}, 마크다운: {}", post.getId(), post.getIsMarkdown());
            return "redirect:/posts/" + post.getId();

        } catch (IllegalArgumentException e) {
            log.warn("게시글 생성 실패 - 오류: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "posts/form";
        }
    }

    /**
     * 게시글 수정 폼
     * GET /posts/{id}/edit
     */
    @GetMapping("/{id}/edit")
    public String getEditPostForm(@PathVariable Long id,
                                 @RequestParam Long userId,
                                 Model model) {
        log.debug("게시글 수정 폼 요청 - ID: {}, 사용자 ID: {}", id, userId);

        try {
            Post post = postService.findById(id);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            if (!post.canEdit(user)) {
                log.warn("게시글 수정 권한 없음 - 게시글 ID: {}, 사용자 ID: {}", id, userId);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "게시글을 수정할 권한이 없습니다.");
            }

            PostUpdateRequest request = PostUpdateRequest.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .category(post.getCategory())
                    .userId(userId)
                    .isMarkdown(post.getIsMarkdown()) // 기존 마크다운 설정 값 로드
                    .build();

            model.addAttribute("post", request);

            return "posts/form";

        } catch (IllegalArgumentException e) {
            log.warn("게시글 수정 폼 조회 실패 - ID: {}, 오류: {}", id, e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * 게시글 수정
     * PUT /posts/{id}
     */
    @PutMapping("/{id}")
    public String updatePost(@PathVariable Long id,
                            @Valid @ModelAttribute("post") PostUpdateRequest request,
                            BindingResult bindingResult,
                            Model model) {
        log.debug("게시글 수정 요청 - ID: {}, 사용자 ID: {}, 마크다운: {}",
                 id, request.getUserId(), request.getIsMarkdown());

        if (bindingResult.hasErrors()) {
            log.warn("게시글 수정 검증 실패 - 오류: {}", bindingResult.getAllErrors());
            return "posts/form";
        }

        try {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            // 마크다운 지원 메서드 사용
            postService.updatePost(id, request.getTitle(), request.getContent(),
                                 request.getCategory(), request.getIsMarkdown(), user);

            log.info("게시글 수정 완료 - ID: {}, 마크다운: {}", id, request.getIsMarkdown());
            return "redirect:/posts/" + id;

        } catch (IllegalArgumentException e) {
            log.warn("게시글 수정 실패 - ID: {}, 오류: {}", id, e.getMessage());

            if (e.getMessage().contains("권한")) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
            } else {
                model.addAttribute("error", e.getMessage());
                return "posts/form";
            }
        }
    }

    /**
     * 게시글 삭제
     * DELETE /posts/{id}
     */
    @DeleteMapping("/{id}")
    public String deletePost(@PathVariable Long id,
                            @RequestParam Long userId) {
        log.debug("게시글 삭제 요청 - ID: {}, 사용자 ID: {}", id, userId);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            postService.deletePost(id, user);

            log.info("게시글 삭제 완료 - ID: {}", id);
            return "redirect:/posts";

        } catch (IllegalArgumentException e) {
            log.warn("게시글 삭제 실패 - ID: {}, 오류: {}", id, e.getMessage());

            if (e.getMessage().contains("권한")) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            }
        }
    }

    /**
     * 게시글 추천
     * POST /posts/{id}/like
     */
    @PostMapping("/{id}/like")
    @ResponseBody
    public Map<String, Object> likePost(@PathVariable Long id, HttpServletRequest request) {
        log.debug("게시글 추천 요청 - ID: {}", id);

        try {
            // 임시로 사용자 ID 1 사용 (실제로는 인증된 사용자 정보 사용)
            Long userId = 1L;

            likeService.likePost(id, userId);

            // 현재 추천 수와 사용자 추천 상태 조회
            Long likeCount = likeService.getLikeCount(id);
            Boolean userLikeStatus = likeService.getUserLikeStatus(id, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("likeCount", likeCount);
            response.put("userLikeStatus", userLikeStatus);

            log.debug("게시글 추천 완료 - ID: {}, 추천수: {}", id, likeCount);
            return response;
        } catch (Exception e) {
            log.error("게시글 추천 실패 - ID: {}, 오류: {}", id, e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        }
    }

    /**
     * 게시글 비추천
     * POST /posts/{id}/dislike
     */
    @PostMapping("/{id}/dislike")
    @ResponseBody
    public Map<String, Object> dislikePost(@PathVariable Long id, HttpServletRequest request) {
        log.debug("게시글 비추천 요청 - ID: {}", id);

        try {
            // 임시로 사용자 ID 1 사용 (실제로는 인증된 사용자 정보 사용)
            Long userId = 1L;

            likeService.dislikePost(id, userId);

            // 현재 추천 수와 사용자 추천 상태 조회
            Long likeCount = likeService.getLikeCount(id);
            Long dislikeCount = likeService.getDislikeCount(id);
            Boolean userLikeStatus = likeService.getUserLikeStatus(id, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("likeCount", likeCount);
            response.put("dislikeCount", dislikeCount);
            response.put("userLikeStatus", userLikeStatus);

            log.debug("게시글 비추천 완료 - ID: {}, 추천수: {}, 비추천수: {}", id, likeCount, dislikeCount);
            return response;
        } catch (Exception e) {
            log.error("게시글 비추천 실패 - ID: {}, 오류: {}", id, e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        }
    }

    /**
     * 추천/비추천 취소
     * DELETE /posts/{id}/like
     */
    @DeleteMapping("/{id}/like")
    @ResponseBody
    public Map<String, Object> cancelLike(@PathVariable Long id, HttpServletRequest request) {
        log.debug("추천/비추천 취소 요청 - ID: {}", id);

        try {
            // 임시로 사용자 ID 1 사용 (실제로는 인증된 사용자 정보 사용)
            Long userId = 1L;

            likeService.cancelLike(id, userId);

            // 현재 추천 수와 사용자 추천 상태 조회
            Long likeCount = likeService.getLikeCount(id);
            Long dislikeCount = likeService.getDislikeCount(id);
            Boolean userLikeStatus = likeService.getUserLikeStatus(id, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("likeCount", likeCount);
            response.put("dislikeCount", dislikeCount);
            response.put("userLikeStatus", userLikeStatus);

            log.debug("추천/비추천 취소 완료 - ID: {}, 추천수: {}, 비추천수: {}", id, likeCount, dislikeCount);
            return response;
        } catch (Exception e) {
            log.error("추천/비추천 취소 실패 - ID: {}, 오류: {}", id, e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        }
    }

    /**
     * 사용자 추천 상태 조회
     * GET /posts/{id}/like-status
     */
    @GetMapping("/{id}/like-status")
    @ResponseBody
    public Map<String, Object> getLikeStatus(@PathVariable Long id, HttpServletRequest request) {
        log.debug("사용자 추천 상태 조회 요청 - ID: {}", id);

        try {
            // 임시로 사용자 ID 1 사용 (실제로는 인증된 사용자 정보 사용)
            Long userId = 1L;

            Long likeCount = likeService.getLikeCount(id);
            Long dislikeCount = likeService.getDislikeCount(id);
            Boolean userLikeStatus = likeService.getUserLikeStatus(id, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("likeCount", likeCount);
            response.put("dislikeCount", dislikeCount);
            response.put("userLikeStatus", userLikeStatus);

            log.debug("사용자 추천 상태 조회 완료 - ID: {}, 상태: {}", id, userLikeStatus);
            return response;
        } catch (Exception e) {
            log.error("사용자 추천 상태 조회 실패 - ID: {}, 오류: {}", id, e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        }
    }

    /**
     * 게시글 생성 요청 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostCreateRequest {
        private Long id; // 템플릿 호환성을 위해 추가 (항상 null)

        @NotBlank(message = "제목은 필수입니다.")
        @jakarta.validation.constraints.Size(max = 200, message = "제목은 200자 이하여야 합니다.")
        private String title;

        @NotBlank(message = "내용은 필수입니다.")
        private String content;

        private String category;

        @NotNull(message = "작성자는 필수입니다.")
        private Long authorId;

        // 마크다운 지원 필드
        @Builder.Default
        private Boolean isMarkdown = false;
    }

    /**
     * 게시글 수정 요청 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostUpdateRequest {
        private Long id;

        @NotBlank(message = "제목은 필수입니다.")
        @jakarta.validation.constraints.Size(max = 200, message = "제목은 200자 이하여야 합니다.")
        private String title;

        @NotBlank(message = "내용은 필수입니다.")
        private String content;

        private String category;

        @NotNull(message = "사용자 ID는 필수입니다.")
        private Long userId;

        // 마크다운 지원 필드
        @Builder.Default
        private Boolean isMarkdown = false;
    }
}
