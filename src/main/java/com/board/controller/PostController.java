package com.board.controller;

import com.board.domain.entity.Post;
import com.board.domain.entity.User;
import com.board.domain.repository.UserRepository;
import com.board.service.PostService;
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

    /**
     * 게시글 목록 조회
     * GET /posts
     */
    @GetMapping
    public String getPosts(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size,
                          Model model) {
        log.debug("게시글 목록 조회 요청 - 페이지: {}, 크기: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postService.findAll(pageable);

        model.addAttribute("posts", posts);

        log.debug("게시글 목록 조회 완료 - 전체: {}, 현재 페이지: {}",
                posts.getTotalElements(), posts.getNumberOfElements());

        return "posts/list";
    }

    /**
     * 게시글 상세 조회
     * GET /posts/{id}
     */
    @GetMapping("/{id}")
    public String getPost(@PathVariable Long id, Model model) {
        log.debug("게시글 상세 조회 요청 - ID: {}", id);

        try {
            Post post = postService.findById(id);
            // 조회수 증가
            post = postService.increaseViewCount(id);

            model.addAttribute("post", post);

            log.debug("게시글 상세 조회 완료 - ID: {}, 제목: {}", post.getId(), post.getTitle());
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
        log.debug("게시글 생성 요청 - 제목: {}, 작성자 ID: {}", request.getTitle(), request.getAuthorId());

        if (bindingResult.hasErrors()) {
            log.warn("게시글 생성 검증 실패 - 오류: {}", bindingResult.getAllErrors());
            return "posts/form";
        }

        try {
            User author = userRepository.findById(request.getAuthorId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            Post post = postService.createPost(
                    request.getTitle(),
                    request.getContent(),
                    request.getCategory(),
                    author
            );

            log.info("게시글 생성 완료 - ID: {}", post.getId());
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
        log.debug("게시글 수정 요청 - ID: {}, 사용자 ID: {}", id, request.getUserId());

        if (bindingResult.hasErrors()) {
            log.warn("게시글 수정 검증 실패 - 오류: {}", bindingResult.getAllErrors());
            return "posts/form";
        }

        try {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            postService.updatePost(id, request.getTitle(), request.getContent(), request.getCategory(), user);

            log.info("게시글 수정 완료 - ID: {}", id);
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
     * 게시글 생성 요청 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PostCreateRequest {
        private Long id; // 템플릿 호환성을 위해 추가 (항상 null)

        @jakarta.validation.constraints.NotBlank(message = "제목은 필수입니다.")
        @jakarta.validation.constraints.Size(max = 200, message = "제목은 200자 이하여야 합니다.")
        private String title;

        @jakarta.validation.constraints.NotBlank(message = "내용은 필수입니다.")
        private String content;

        private String category;

        @jakarta.validation.constraints.NotNull(message = "작성자는 필수입니다.")
        private Long authorId;
    }

    /**
     * 게시글 수정 요청 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PostUpdateRequest {
        private Long id;

        @jakarta.validation.constraints.NotBlank(message = "제목은 필수입니다.")
        @jakarta.validation.constraints.Size(max = 200, message = "제목은 200자 이하여야 합니다.")
        private String title;

        @jakarta.validation.constraints.NotBlank(message = "내용은 필수입니다.")
        private String content;

        private String category;

        @jakarta.validation.constraints.NotNull(message = "사용자 ID는 필수입니다.")
        private Long userId;
    }
}
