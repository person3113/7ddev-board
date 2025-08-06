package com.board.integration;

import com.board.domain.entity.Post;
import com.board.domain.entity.User;
import com.board.domain.entity.Comment;
import com.board.domain.enums.Role;
import com.board.domain.repository.PostRepository;
import com.board.domain.repository.UserRepository;
import com.board.domain.repository.CommentRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("전체 플로우 E2E 통합 테스트")
class FullFlowE2ETest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    @DisplayName("전체 플로우 E2E 테스트: 게시글 생성 → 조회 → 댓글 작성 → 검색 → 수정 → 삭제")
    void fullFlowE2ETest() throws Exception {
        // 1. 게시글 목록 조회 (초기 상태)
        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"));

        // 2. 게시글 작성 폼 조회
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"));

        // 3. 게시글 생성 (실제 PostController 파라미터에 맞춰 수정)
        mockMvc.perform(post("/posts")
                        .param("title", "E2E 테스트 게시글")
                        .param("content", "E2E 테스트를 위한 게시글 내용입니다.")
                        .param("category", "테스트")
                        .param("authorId", testUser.getId().toString())) // authorId 추가
                .andExpect(status().is3xxRedirection());

        // 게시글이 정상 생성되었는지 확인
        Post createdPost = postRepository.findAll().stream()
                .filter(p -> "E2E 테스트 게시글".equals(p.getTitle()))
                .findFirst()
                .orElseThrow();

        // 4. 게시글 상세 조회
        mockMvc.perform(get("/posts/" + createdPost.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"));

        // 5. 댓글 작성 (AJAX 방식으로 수정)
        mockMvc.perform(post("/posts/" + createdPost.getId() + "/comments")
                        .contentType("application/json")
                        .content("{\"content\":\"E2E 테스트 댓글입니다.\",\"authorId\":" + testUser.getId() + "}"))
                .andExpect(status().isCreated()); // 201 Created 응답

        // 6. 검색 기능 테스트
        mockMvc.perform(get("/posts/search")
                        .param("searchType", "title")
                        .param("keyword", "E2E"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/search"));

        // 7. 게시글 수정 폼 조회
        mockMvc.perform(get("/posts/" + createdPost.getId() + "/edit")
                        .param("userId", testUser.getId().toString())) // userId 추가
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"));

        // 8. 게시글 수정
        mockMvc.perform(put("/posts/" + createdPost.getId())
                        .param("title", "수정된 E2E 테스트 게시글")
                        .param("content", "수정된 내용입니다.")
                        .param("category", "수정된테스트")
                        .param("userId", testUser.getId().toString())) // userId 추가
                .andExpect(status().is3xxRedirection());

        // 9. 게시글 삭제
        mockMvc.perform(delete("/posts/" + createdPost.getId())
                        .param("userId", testUser.getId().toString())) // userId 추가
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("페이지네이션 플로우 테스트")
    void paginationFlowTest() throws Exception {
        // 테스트 데이터 생성 (15개 게시글)
        for (int i = 1; i <= 15; i++) {
            Post post = Post.builder()
                    .title("테스트 게시글 " + i)
                    .content("테스트 내용 " + i)
                    .category("테스트")
                    .author(testUser)
                    .build();
            postRepository.save(post);
        }

        // 1페이지 조회 (10개)
        mockMvc.perform(get("/posts")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attributeExists("posts"));

        // 2페이지 조회 (5개)
        mockMvc.perform(get("/posts")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attributeExists("posts"));
    }

    @Test
    @DisplayName("검색 플로우 테스트")
    void searchFlowTest() throws Exception {
        // 다양한 테스트 데이터 생성
        Post post1 = Post.builder()
                .title("Spring Boot 튜토리얼")
                .content("Spring 프레임워크 학습")
                .category("개발")
                .author(testUser)
                .build();

        Post post2 = Post.builder()
                .title("Java 기초")
                .content("자바 프로그래밍 언어")
                .category("프로그래밍")
                .author(testUser)
                .build();

        postRepository.save(post1);
        postRepository.save(post2);

        // 제목 검색 테스트
        mockMvc.perform(get("/posts/search")
                        .param("searchType", "title")
                        .param("keyword", "Spring"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/search"));

        // 내용 검색 테스트
        mockMvc.perform(get("/posts/search")
                        .param("searchType", "content")
                        .param("keyword", "자바"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/search"));

        // 카테고리 검색 테스트
        mockMvc.perform(get("/posts/search")
                        .param("searchType", "category")
                        .param("keyword", "개발"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/search"));

        // 복합 검색 테스트
        mockMvc.perform(get("/posts/search")
                        .param("searchType", "title_content")
                        .param("keyword", "Java"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/search"));
    }
}
