package com.board.controller;

import com.board.domain.entity.Post;
import com.board.domain.entity.User;
import com.board.domain.enums.Role;
import com.board.domain.repository.PostRepository;
import com.board.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PostController 통합 테스트
 *
 * TDD Red 단계: 실패하는 테스트를 먼저 작성
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .nickname("테스트유저")
                .role(Role.USER)
                .build();
        testUser = userRepository.save(testUser);

        // 테스트 게시글 생성
        testPost = Post.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .category("일반")
                .author(testUser)
                .build();
        testPost = postRepository.save(testPost);
    }

    @Test
    @DisplayName("GET /posts - 게시글 목록 조회")
    void getPosts() throws Exception {
        mockMvc.perform(get("/posts"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attribute("posts", hasProperty("content", hasSize(greaterThan(0)))));
    }

    @Test
    @DisplayName("GET /posts/{id} - 게시글 상세 조회")
    void getPost() throws Exception {
        mockMvc.perform(get("/posts/{id}", testPost.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attribute("post", hasProperty("title", is("테스트 게시글"))));
    }

    @Test
    @DisplayName("GET /posts/{id} - 존재하지 않는 게시글 조회 시 404")
    void getPost_NotFound() throws Exception {
        mockMvc.perform(get("/posts/{id}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /posts/new - 게시글 작성 폼")
    void getNewPostForm() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeExists("post"));
    }

    @Test
    @DisplayName("POST /posts - 게시글 생성")
    void createPost() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("title", "새 게시글")
                        .param("content", "새 게시글 내용")
                        .param("category", "공지")
                        .param("authorId", testUser.getId().toString()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/posts/*"));
    }

    @Test
    @DisplayName("POST /posts - 필수 필드 누락 시 실패")
    void createPost_ValidationFail() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("title", "")  // 제목 누락
                        .param("content", "내용")
                        .param("authorId", testUser.getId().toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"));
    }

    @Test
    @DisplayName("GET /posts/{id}/edit - 게시글 수정 폼")
    void getEditPostForm() throws Exception {
        mockMvc.perform(get("/posts/{id}/edit", testPost.getId())
                        .param("userId", testUser.getId().toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attribute("post", hasProperty("title", is("테스트 게시글"))));
    }

    @Test
    @DisplayName("PUT /posts/{id} - 게시글 수정")
    void updatePost() throws Exception {
        mockMvc.perform(put("/posts/{id}", testPost.getId())
                        .param("title", "수정된 제목")
                        .param("content", "수정된 내용")
                        .param("category", "수정된 카테고리")
                        .param("userId", testUser.getId().toString()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + testPost.getId()));
    }

    @Test
    @DisplayName("PUT /posts/{id} - 권한 없는 사용자의 수정 시도")
    void updatePost_Unauthorized() throws Exception {
        User anotherUser = User.builder()
                .username("another")
                .email("another@example.com")
                .password("password")
                .nickname("다른유저")
                .role(Role.USER)
                .build();
        anotherUser = userRepository.save(anotherUser);

        mockMvc.perform(put("/posts/{id}", testPost.getId())
                        .param("title", "수정된 제목")
                        .param("content", "수정된 내용")
                        .param("userId", anotherUser.getId().toString()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /posts/{id} - 게시글 삭제")
    void deletePost() throws Exception {
        mockMvc.perform(delete("/posts/{id}", testPost.getId())
                        .param("userId", testUser.getId().toString()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    @DisplayName("DELETE /posts/{id} - 권한 없는 사용자의 삭제 시도")
    void deletePost_Unauthorized() throws Exception {
        User anotherUser = User.builder()
                .username("another2")
                .email("another2@example.com")
                .password("password")
                .nickname("다른유저2")
                .role(Role.USER)
                .build();
        anotherUser = userRepository.save(anotherUser);

        mockMvc.perform(delete("/posts/{id}", testPost.getId())
                        .param("userId", anotherUser.getId().toString()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /posts - 페이지네이션 테스트")
    void getPosts_WithPagination() throws Exception {
        mockMvc.perform(get("/posts")
                        .param("page", "0")
                        .param("size", "5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attributeExists("posts"));
    }
}
