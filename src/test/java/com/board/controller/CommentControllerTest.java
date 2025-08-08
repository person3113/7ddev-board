package com.board.controller;

import com.board.domain.entity.Comment;
import com.board.domain.entity.Post;
import com.board.domain.entity.User;
import com.board.domain.enums.Role;
import com.board.domain.repository.CommentRepository;
import com.board.domain.repository.PostRepository;
import com.board.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private User author;
    private User otherUser;
    private Post post;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        author = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .nickname("테스트유저")
                .role(Role.USER)
                .build();
        userRepository.save(author);

        otherUser = User.builder()
                .username("otheruser")
                .email("other@example.com")
                .password("password123")
                .nickname("다른유저")
                .role(Role.USER)
                .build();
        userRepository.save(otherUser);

        // 테스트 게시글 생성
        post = Post.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .category("테스트")
                .author(author)
                .build();
        postRepository.save(post);
    }

    @Test
    @DisplayName("댓글 생성 - POST /posts/{postId}/comments (폼 제출)")
    @WithMockUser(username = "testuser", roles = "USER")
    void createComment_Success() throws Exception {
        // when & then
        mockMvc.perform(post("/posts/{postId}/comments", post.getId())
                        .param("content", "테스트 댓글 내용")
                        .param("authorId", author.getId().toString())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + post.getId()));
    }

    @Test
    @DisplayName("빈 내용으로 댓글 생성 - 400")
    @WithMockUser(username = "testuser", roles = "USER")
    void createComment_EmptyContent() throws Exception {
        // when & then
        mockMvc.perform(post("/posts/{postId}/comments", post.getId())
                        .param("content", "")
                        .param("authorId", author.getId().toString())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + post.getId() + "?error=empty_content"));
    }

    @Test
    @DisplayName("댓글 생성 API - POST /api/posts/{postId}/comments (AJAX)")
    @WithMockUser(username = "testuser", roles = "USER")
    void createCommentApi_Success() throws Exception {
        // given
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("content", "테스트 댓글 내용");
        requestBody.put("authorId", author.getId());

        // when & then
        mockMvc.perform(post("/api/posts/{postId}/comments", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("테스트 댓글 내용"))
                .andExpect(jsonPath("$.author.username").value("testuser"))
                .andExpect(jsonPath("$.post.id").value(post.getId()));
    }

    @Test
    @DisplayName("대댓글 생성 - POST /comments/{commentId}/replies")
    @WithMockUser(username = "testuser", roles = "USER")
    void createReply_Success() throws Exception {
        // given
        Comment parentComment = Comment.builder()
                .content("부모 댓글")
                .post(post)
                .author(author)
                .build();
        commentRepository.save(parentComment);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("content", "대댓글 내용");
        requestBody.put("authorId", otherUser.getId());

        // when & then
        mockMvc.perform(post("/comments/{commentId}/replies", parentComment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("대댓글 내용"))
                .andExpect(jsonPath("$.author.username").value("otheruser"))
                .andExpect(jsonPath("$.parent.id").value(parentComment.getId()));
    }

    @Test
    @DisplayName("댓글 목록 조회 - GET /posts/{postId}/comments")
    void getComments_Success() throws Exception {
        // given
        Comment comment1 = Comment.builder()
                .content("첫 번째 댓글")
                .post(post)
                .author(author)
                .build();
        commentRepository.save(comment1);

        Comment comment2 = Comment.builder()
                .content("두 번째 댓글")
                .post(post)
                .author(otherUser)
                .build();
        commentRepository.save(comment2);

        // when & then
        mockMvc.perform(get("/posts/{postId}/comments", post.getId())
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].content").value("첫 번째 댓글"))
                .andExpect(jsonPath("$.content[1].content").value("두 번째 댓글"));
    }

    @Test
    @DisplayName("댓글 단건 조회 - GET /comments/{id}")
    void getComment_Success() throws Exception {
        // given
        Comment comment = Comment.builder()
                .content("테스트 댓글")
                .post(post)
                .author(author)
                .build();
        commentRepository.save(comment);

        // when & then
        mockMvc.perform(get("/comments/{id}", comment.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("테스트 댓글"))
                .andExpect(jsonPath("$.author.username").value("testuser"));
    }

    @Test
    @DisplayName("댓글 수정 - PUT /comments/{id}")
    @WithMockUser(username = "testuser", roles = "USER")
    void updateComment_Success() throws Exception {
        // given
        Comment comment = Comment.builder()
                .content("원본 댓글")
                .post(post)
                .author(author)
                .build();
        commentRepository.save(comment);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("content", "수정된 댓글");
        requestBody.put("authorId", author.getId());

        // when & then
        mockMvc.perform(put("/comments/{id}", comment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("수정된 댓글"));
    }

    @Test
    @DisplayName("댓글 수정 - 권한 없음 403")
    @WithMockUser(username = "otheruser", roles = "USER")
    void updateComment_Forbidden() throws Exception {
        // given
        Comment comment = Comment.builder()
                .content("원본 댓글")
                .post(post)
                .author(author)
                .build();
        commentRepository.save(comment);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("content", "수정된 댓글");
        requestBody.put("authorId", otherUser.getId()); // 다른 사용자

        // when & then
        mockMvc.perform(put("/comments/{id}", comment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("댓글 삭제 - DELETE /comments/{id}")
    @WithMockUser(username = "testuser", roles = "USER")
    void deleteComment_Success() throws Exception {
        // given
        Comment comment = Comment.builder()
                .content("삭제할 댓글")
                .post(post)
                .author(author)
                .build();
        commentRepository.save(comment);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("authorId", author.getId());

        // when & then
        mockMvc.perform(delete("/comments/{id}", comment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("댓글 삭제 - 권한 없음 403")
    @WithMockUser(username = "otheruser", roles = "USER")
    void deleteComment_Forbidden() throws Exception {
        // given
        Comment comment = Comment.builder()
                .content("삭제할 댓글")
                .post(post)
                .author(author)
                .build();
        commentRepository.save(comment);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("authorId", otherUser.getId()); // 다른 사용자

        // when & then
        mockMvc.perform(delete("/comments/{id}", comment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("존재하지 않는 댓글 조회 - 404")
    void getComment_NotFound() throws Exception {
        // given
        Long nonExistentId = 999L;

        // when & then
        mockMvc.perform(get("/comments/{id}", nonExistentId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
