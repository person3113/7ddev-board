package com.board.controller;

import com.board.domain.entity.User;
import com.board.domain.enums.Role;
import com.board.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerSessionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private User createTestUser(Long id, String username, String nickname) throws Exception {
        User user = User.builder()
                .username(username)
                .email("test@example.com")
                .password("password123")
                .nickname(nickname)
                .role(Role.USER)
                .build();

        // 리플렉션을 사용하여 id 설정
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, id);

        return user;
    }

    @Test
    @DisplayName("로그인 성공 시 세션에 사용자 정보가 저장되어야 한다")
    void loginSuccess_ShouldStoreUserInSession() throws Exception {
        // Given
        User mockUser = createTestUser(2L, "testuser", "테스트사용자");

        when(userService.authenticateUser("testuser", "password123"))
                .thenReturn(mockUser);

        // When & Then
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/login")
                        .param("username", "testuser")
                        .param("password", "password123")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"))
                .andExpect(request().sessionAttribute("currentUser", mockUser));
    }

    @Test
    @DisplayName("잘못된 로그인 정보로 로그인 시 실패해야 한다")
    void loginFail_ShouldReturnToLoginForm() throws Exception {
        // Given
        when(userService.authenticateUser(anyString(), anyString()))
                .thenReturn(null);

        // When & Then
        mockMvc.perform(post("/login")
                        .param("username", "wronguser")
                        .param("password", "wrongpassword"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @DisplayName("로그아웃 시 세션이 삭제되어야 한다")
    void logout_ShouldClearSession() throws Exception {
        // Given
        MockHttpSession session = new MockHttpSession();
        User mockUser = createTestUser(2L, "testuser", "테스트사용자");
        session.setAttribute("currentUser", mockUser);

        // When & Then
        mockMvc.perform(post("/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
