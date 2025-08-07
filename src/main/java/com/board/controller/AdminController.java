package com.board.controller;

import com.board.domain.entity.Comment;
import com.board.domain.entity.Post;
import com.board.domain.entity.User;
import com.board.domain.enums.Role;
import com.board.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 관리자 대시보드
     */
    @GetMapping
    public String adminDashboard(Authentication auth, Model model) {
        String username = auth.getName();
        
        AdminService.AdminStats stats = adminService.getAdminStats(username);
        model.addAttribute("stats", stats);
        
        return "admin/dashboard";
    }

    /**
     * 게시글 관리 페이지
     */
    @GetMapping("/posts")
    public String managePosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication auth,
            Model model) {

        String username = auth.getName();
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Post> posts = adminService.getAllPosts(username, pageable);
        model.addAttribute("posts", posts);
        model.addAttribute("currentPage", page);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        
        return "admin/posts";
    }

    /**
     * 댓글 관리 페이지
     */
    @GetMapping("/comments")
    public String manageComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication auth,
            Model model) {

        String username = auth.getName();
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Comment> comments = adminService.getAllComments(username, pageable);
        model.addAttribute("comments", comments);
        model.addAttribute("currentPage", page);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        
        return "admin/comments";
    }

    /**
     * 사용자 관리 페이지
     */
    @GetMapping("/users")
    public String manageUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication auth,
            Model model) {

        String username = auth.getName();
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<User> users = adminService.getAllUsers(username, pageable);
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("roles", Role.values());
        
        return "admin/users";
    }

    /**
     * 게시글 강제 삭제
     */
    @PostMapping("/posts/{id}/delete")
    public String forceDeletePost(
            @PathVariable Long id,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        String username = auth.getName();
        
        try {
            adminService.forceDeletePost(id, username);
            redirectAttributes.addFlashAttribute("successMessage", "게시글이 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "게시글 삭제에 실패했습니다: " + e.getMessage());
        }
        
        return "redirect:/admin/posts";
    }

    /**
     * 댓글 강제 삭제
     */
    @PostMapping("/comments/{id}/delete")
    public String forceDeleteComment(
            @PathVariable Long id,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        String username = auth.getName();
        
        try {
            adminService.forceDeleteComment(id, username);
            redirectAttributes.addFlashAttribute("successMessage", "댓글이 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "댓글 삭제에 실패했습니다: " + e.getMessage());
        }
        
        return "redirect:/admin/comments";
    }

    /**
     * 사용자 권한 변경
     */
    @PostMapping("/users/{username}/role")
    public String changeUserRole(
            @PathVariable String username,
            @RequestParam Role role,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        String adminUsername = auth.getName();
        
        try {
            adminService.changeUserRole(username, role, adminUsername);
            redirectAttributes.addFlashAttribute("successMessage", "사용자 권한이 변경되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "권한 변경에 실패했습니다: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
}
