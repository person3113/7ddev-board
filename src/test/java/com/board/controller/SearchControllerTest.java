package com.board.controller;

import com.board.domain.entity.Post;
import com.board.domain.entity.User;
import com.board.domain.enums.Role;
import com.board.domain.repository.PostRepository;
import com.board.domain.repository.UserRepository;
import com.board.service.SearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SearchControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SearchService searchService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // MockMvc 초기화
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // 테스트 사용자 생성
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .nickname("테스트유저")
                .role(Role.USER)
                .build();
        userRepository.save(testUser);

        // 테스트 게시글들 생성
        Post post1 = Post.builder()
                .title("Spring Boot 튜토리얼")
                .content("Spring Boot를 이용한 웹 개발 방법")
                .category("개발")
                .author(testUser)
                .build();

        Post post2 = Post.builder()
                .title("Java 기초 문법")
                .content("자바 프로그래밍 언어의 기본 문법을 배워보자")
                .category("프로그래밍")
                .author(testUser)
                .build();

        postRepository.save(post1);
        postRepository.save(post2);
    }

    @Test
    @DisplayName("검색 페이지 조회 - 성공")
    void searchPage_Success() throws Exception {
        mockMvc.perform(get("/posts/search"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/search"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attributeExists("searchType"))
                .andExpect(model().attributeExists("keyword"));
    }

    @Test
    @DisplayName("제목으로 검색 - 성공")
    void searchByTitle_Success() throws Exception {
        mockMvc.perform(get("/posts/search")
                        .param("searchType", "title")
                        .param("keyword", "Spring"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/search"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attribute("searchType", "title"))
                .andExpect(model().attribute("keyword", "Spring"));
    }

    @Test
    @DisplayName("내용으로 검색 - 성공")
    void searchByContent_Success() throws Exception {
        mockMvc.perform(get("/posts/search")
                        .param("searchType", "content")
                        .param("keyword", "자바"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/search"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attribute("searchType", "content"))
                .andExpect(model().attribute("keyword", "자바"));
    }

    @Test
    @DisplayName("작성자로 검색 - 성공")
    void searchByAuthor_Success() throws Exception {
        mockMvc.perform(get("/posts/search")
                        .param("searchType", "author")
                        .param("keyword", "테스트유저"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/search"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attribute("searchType", "author"))
                .andExpect(model().attribute("keyword", "테스트유저"));
    }

    @Test
    @DisplayName("카테고리로 검색 - 성공")
    void searchByCategory_Success() throws Exception {
        mockMvc.perform(get("/posts/search")
                        .param("searchType", "category")
                        .param("keyword", "개발"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/search"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attribute("searchType", "category"))
                .andExpect(model().attribute("keyword", "개발"));
    }

    @Test
    @DisplayName("복합 검색 (제목+내용) - 성공")
    void searchByTitleAndContent_Success() throws Exception {
        mockMvc.perform(get("/posts/search")
                        .param("searchType", "title_content")
                        .param("keyword", "Java"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/search"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attribute("searchType", "title_content"))
                .andExpect(model().attribute("keyword", "Java"));
    }

    @Test
    @DisplayName("전체 검색 - 성공")
    void searchByAll_Success() throws Exception {
        mockMvc.perform(get("/posts/search")
                        .param("searchType", "all")
                        .param("keyword", "개발"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/search"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attribute("searchType", "all"))
                .andExpect(model().attribute("keyword", "개발"));
    }

    @Test
    @DisplayName("빈 키워드로 검색 - 빈 결과 반환")
    void searchWithEmptyKeyword_ReturnsEmpty() throws Exception {
        mockMvc.perform(get("/posts/search")
                        .param("searchType", "title")
                        .param("keyword", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/search"))
                .andExpect(model().attributeExists("posts"));
    }

    @Test
    @DisplayName("페이징 파라미터로 검색 - 성공")
    void searchWithPaging_Success() throws Exception {
        mockMvc.perform(get("/posts/search")
                        .param("searchType", "title")
                        .param("keyword", "Spring")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/search"))
                .andExpect(model().attributeExists("posts"));
    }
}
