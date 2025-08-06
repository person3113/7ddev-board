package com.board.integration;

import com.board.domain.entity.Post;
import com.board.domain.entity.User;
import com.board.domain.enums.Role;
import com.board.domain.repository.PostRepository;
import com.board.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("에러 시나리오 테스트 (간소화)")
class ErrorScenarioTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // 테스트 사용자 생성
        testUser = User.builder()
                .username("erroruser")
                .email("error@example.com")
                .password("password")
                .nickname("에러테스트유저")
                .role(Role.USER)
                .build();
        userRepository.save(testUser);

        // 테스트 게시글 생성
        testPost = Post.builder()
                .title("에러 테스트 게시글")
                .content("에러 시나리오 테스트용 게시글")
                .category("테스트")
                .author(testUser)
                .build();
        postRepository.save(testPost);
    }

    @Test
    @DisplayName("404 에러 - 존재하지 않는 게시글 조회")
    void notFoundPostTest() throws Exception {
        Long nonExistentId = 99999L;

        mockMvc.perform(get("/posts/" + nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("검색 - 빈 키워드로 검색 시 정상 처리")
    void searchWithEmptyKeywordTest() throws Exception {
        mockMvc.perform(get("/posts/search")
                        .param("searchType", "title")
                        .param("keyword", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/search"));
    }

    @Test
    @DisplayName("기본 페이지 접근 테스트")
    void basicPageAccessTest() throws Exception {
        // 게시글 목록 페이지
        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"));

        // 게시글 작성 폼
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"));

        // 검색 페이지
        mockMvc.perform(get("/posts/search"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/search"));
    }

    @Test
    @DisplayName("유효성 검증 실패 - 빈 제목으로 게시글 생성")
    void validationFailureEmptyTitleTest() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("title", "")  // 빈 제목
                        .param("content", "내용은 있음")
                        .param("category", "테스트")
                        .param("authorId", testUser.getId().toString()))
                .andExpect(status().isOk())  // 200 (form으로 다시 이동)
                .andExpect(view().name("posts/form"));
    }

    @Test
    @DisplayName("유효성 검증 실패 - 빈 내용으로 게시글 생성")
    void validationFailureEmptyContentTest() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("title", "제목은 있음")
                        .param("content", "")  // 빈 내용
                        .param("category", "테스트")
                        .param("authorId", testUser.getId().toString()))
                .andExpect(status().isOk())  // 200 (form으로 다시 이동)
                .andExpect(view().name("posts/form"));
    }

    @Test
    @DisplayName("정상적인 게시글 CRUD 플로우")
    void normalPostCrudFlow() throws Exception {
        // 게시글 생성
        mockMvc.perform(post("/posts")
                        .param("title", "정상 게시글")
                        .param("content", "정상 내용")
                        .param("category", "정상")
                        .param("authorId", testUser.getId().toString()))
                .andExpect(status().is3xxRedirection());

        // 생성된 게시글 조회
        Post createdPost = postRepository.findAll().stream()
                .filter(p -> "정상 게시글".equals(p.getTitle()))
                .findFirst()
                .orElseThrow();

        // 게시글 상세 조회
        mockMvc.perform(get("/posts/" + createdPost.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"));

        // 게시글 수정 폼
        mockMvc.perform(get("/posts/" + createdPost.getId() + "/edit")
                        .param("userId", testUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"));

        // 게시글 수정
        mockMvc.perform(put("/posts/" + createdPost.getId())
                        .param("title", "수정된 게시글")
                        .param("content", "수정된 내용")
                        .param("category", "수정")
                        .param("userId", testUser.getId().toString()))
                .andExpect(status().is3xxRedirection());

        // 게시글 삭제
        mockMvc.perform(delete("/posts/" + createdPost.getId())
                        .param("userId", testUser.getId().toString()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("파라미터 누락 에러 - authorId 없이 게시글 생성 (빈 제목)")
    void missingParameterEmptyTitleTest() throws Exception {
        // authorId 없이 요청 - 실제 동작 확인 후 수정
        mockMvc.perform(post("/posts")
                        .param("title", "")  // 빈 제목
                        .param("content", "내용은 있음")
                        .param("category", "테스트"))
                .andExpect(status().isOk())  // 실제로는 폼으로 리다이렉트될 가능성
                .andExpect(view().name("posts/form"));
    }

    @Test
    @DisplayName("파라미터 누락 에러 - authorId 없이 게시글 생성 (빈 내용)")
    void missingParameterEmptyContentTest() throws Exception {
        // authorId 없이 요청 - 실제 동작 확인 후 수정
        mockMvc.perform(post("/posts")
                        .param("title", "제목은 있음")
                        .param("content", "")  // 빈 내용
                        .param("category", "테스트"))
                .andExpect(status().isOk())  // 실제로는 폼으로 리다이렉트될 가능성
                .andExpect(view().name("posts/form"));
    }

    @Test
    @DisplayName("유효성 검증 실패 - authorId 없이 게시글 생성 (빈 제목)")
    void validationFailureEmptyTitleWithoutAuthorTest() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("title", "")  // 빈 제목
                        .param("content", "내용은 있음")
                        .param("category", "테스트"))
                .andExpect(status().isOk())  // 200 (form으로 리다이렉트)
                .andExpect(view().name("posts/form"));
    }

    @Test
    @DisplayName("유효성 검증 실패 - authorId 없이 게시글 생성 (빈 내용)")
    void validationFailureEmptyContentWithoutAuthorTest() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("title", "제목은 있음")
                        .param("content", "")  // 빈 내용
                        .param("category", "테스트"))
                .andExpect(status().isOk())  // 200 (form으로 리다이렉트)
                .andExpect(view().name("posts/form"));
    }
}
