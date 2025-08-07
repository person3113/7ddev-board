package com.board.controller;

import com.board.domain.entity.Comment;
import com.board.domain.entity.Post;
import com.board.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;

    /**
     * 사용자 프로필 페이지
     */
    @GetMapping("/{username}")
    public String getUserProfile(
            @PathVariable String username,
            @RequestParam(defaultValue = "posts") String tab,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        // 사용자 프로필 정보 조회
        UserProfileService.UserProfile userProfile = userProfileService.getUserProfile(username);
        model.addAttribute("userProfile", userProfile);

        // 페이징 설정
        Pageable pageable = PageRequest.of(page, size);

        // 탭에 따라 다른 데이터 조회
        if ("comments".equals(tab)) {
            Page<Comment> comments = userProfileService.getUserComments(username, pageable);
            model.addAttribute("comments", comments);
            model.addAttribute("currentTab", "comments");
        } else {
            // 기본값은 게시글 탭
            Page<Post> posts = userProfileService.getUserPosts(username, pageable);
            model.addAttribute("posts", posts);
            model.addAttribute("currentTab", "posts");
        }

        // 사용자 통계 정보
        UserProfileService.UserStats userStats = userProfileService.getUserStats(username);
        model.addAttribute("userStats", userStats);

        // 페이징 정보
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);

        return "users/profile";
    }
}
