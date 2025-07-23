package com.board.domain.entity;

import com.board.domain.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User 엔티티 테스트")
class UserTest {

    @Test
    @DisplayName("사용자를 정상적으로 생성할 수 있다")
    void createUser() {
        // given
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";
        String nickname = "테스트유저";

        // when
        User user = User.builder()
                .username(username)
                .email(email)
                .password(password)
                .nickname(nickname)
                .role(Role.USER)
                .build();

        // then
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getNickname()).isEqualTo(nickname);
        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("username이 null이면 예외가 발생한다")
    void createUserWithNullUsername() {
        // when & then
        assertThatThrownBy(() -> User.builder()
                .username(null)
                .email("test@example.com")
                .password("password123")
                .nickname("테스트유저")
                .role(Role.USER)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username은 필수입니다");
    }

    @Test
    @DisplayName("email이 null이면 예외가 발생한다")
    void createUserWithNullEmail() {
        // when & then
        assertThatThrownBy(() -> User.builder()
                .username("testuser")
                .email(null)
                .password("password123")
                .nickname("테스트유저")
                .role(Role.USER)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email은 필수입니다");
    }

    @Test
    @DisplayName("사용자 정보를 수정할 수 있다")
    void updateUser() {
        // given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .nickname("테스트유저")
                .role(Role.USER)
                .build();

        String newNickname = "새로운닉네임";
        String newEmail = "newemail@example.com";

        // when
        user.updateProfile(newNickname, newEmail);

        // then
        assertThat(user.getNickname()).isEqualTo(newNickname);
        assertThat(user.getEmail()).isEqualTo(newEmail);
    }
}
