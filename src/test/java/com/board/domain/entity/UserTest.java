package com.board.domain.entity;

import com.board.domain.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

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
        assertThat(user.getLastLogin()).isNull(); // 회원가입 시에는 null
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

    @Test
    @DisplayName("로그인 시간을 업데이트할 수 있다")
    void updateLastLogin() {
        // given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .nickname("테스트유저")
                .role(Role.USER)
                .build();

        LocalDateTime beforeUpdate = LocalDateTime.now();

        // when
        user.updateLastLogin();

        // then
        assertThat(user.getLastLogin()).isNotNull();
        assertThat(user.getLastLogin()).isAfterOrEqualTo(beforeUpdate);
    }

    @Test
    @DisplayName("휴면 계정을 체크할 수 있다")
    void checkDormantAccount() {
        // given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .nickname("테스트유저")
                .role(Role.USER)
                .build();

        // when & then - 한 번도 로그인하지 않은 경우
        assertThat(user.isDormant()).isFalse();

        // 31일 전 로그인 (휴면 계정)
        user.updateLastLogin();
        ReflectionTestUtils.setField(user, "lastLogin", LocalDateTime.now().minusDays(31));
        assertThat(user.isDormant()).isTrue();

        // 최근 로그인 (활성 계정)
        user.updateLastLogin();
        assertThat(user.isDormant()).isFalse();
    }

    @Test
    @DisplayName("신규 가입자를 체크할 수 있다")
    void checkNewUser() {
        // given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .nickname("테스트유저")
                .role(Role.USER)
                .build();

        // when & then - 신규 가입자
        assertThat(user.isNewUser()).isTrue();

        // 8일 전 가입자 (신규 아님)
        ReflectionTestUtils.setField(user, "createdAt", LocalDateTime.now().minusDays(8));
        assertThat(user.isNewUser()).isFalse();
    }

    @Test
    @DisplayName("관리자 권한을 체크할 수 있다")
    void checkAdminRole() {
        // given
        User normalUser = User.builder()
                .username("user")
                .email("user@example.com")
                .password("password123")
                .nickname("일반유저")
                .role(Role.USER)
                .build();

        User adminUser = User.builder()
                .username("admin")
                .email("admin@example.com")
                .password("password123")
                .nickname("관리자")
                .role(Role.ADMIN)
                .build();

        // when & then
        assertThat(normalUser.isAdmin()).isFalse();
        assertThat(adminUser.isAdmin()).isTrue();
    }
}
