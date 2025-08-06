package com.board.controller;

import com.board.domain.entity.Post;
import com.board.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * 검색 페이지 및 검색 결과 조회
     */
    @GetMapping("/search")
    public String search(
            @RequestParam(defaultValue = "title_content") String searchType,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "") String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        // 페이징 설정
        Pageable pageable = PageRequest.of(page, size);

        // 검색 실행
        Page<Post> searchResults = searchService.search(searchType, keyword, category, author, pageable);

        // 모델에 데이터 추가
        model.addAttribute("posts", searchResults);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("author", author);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", searchResults.getTotalPages());
        model.addAttribute("totalElements", searchResults.getTotalElements());
        model.addAttribute("hasNext", searchResults.hasNext());
        model.addAttribute("hasPrevious", searchResults.hasPrevious());

        return "posts/search";
    }
}
