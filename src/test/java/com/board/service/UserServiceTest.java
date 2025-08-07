package com.board.service;

import com.board.domain.entity.User;
import com.board.domain.enums.Role;
import com.board.domain.repository.UserRepository;
import com.board.exception.DuplicateResourceException;
import com.board.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .nickname("테스트유저")
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("회원가입 성공 - 유효한 데이터로 가입")
    void registerUser_Success() {
        // given
        String rawPassword = "password123";
        String encodedPassword = "encoded_password123";

        given(userRepository.existsByUsername(testUser.getUsername())).willReturn(false);
        given(userRepository.existsByEmail(testUser.getEmail())).willReturn(false);
        given(passwordEncoder.encode(rawPassword)).willReturn(encodedPassword);
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // when
        User savedUser = userService.registerUser(
                testUser.getUsername(),
                testUser.getEmail(),
                rawPassword,
                testUser.getNickname()
        );

        // then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo(testUser.getUsername());
        assertThat(savedUser.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);

        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 username")
    void registerUser_Fail_DuplicateUsername() {
        // given
        given(userRepository.existsByUsername(testUser.getUsername())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.registerUser(
                testUser.getUsername(),
                testUser.getEmail(),
                "password123",
                testUser.getNickname()
        )).isInstanceOf(DuplicateResourceException.class)
          .hasMessage("이미 존재하는 username입니다: " + testUser.getUsername());
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 email")
    void registerUser_Fail_DuplicateEmail() {
        // given
        given(userRepository.existsByUsername(testUser.getUsername())).willReturn(false);
        given(userRepository.existsByEmail(testUser.getEmail())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.registerUser(
                testUser.getUsername(),
                testUser.getEmail(),
                "password123",
                testUser.getNickname()
        )).isInstanceOf(DuplicateResourceException.class)
          .hasMessage("이미 존재하는 email입니다: " + testUser.getEmail());
    }

    @Test
    @DisplayName("비밀번호 암호화 확인")
    void registerUser_PasswordEncrypted() {
        // given
        String rawPassword = "password123";
        String encodedPassword = "encoded_password123";

        given(userRepository.existsByUsername(anyString())).willReturn(false);
        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(passwordEncoder.encode(rawPassword)).willReturn(encodedPassword);
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertThat(user.getPassword()).isEqualTo(encodedPassword);
            return user;
        });

        // when
        userService.registerUser("testuser", "test@test.com", rawPassword, "테스트");

        // then
        verify(passwordEncoder).encode(rawPassword);
    }

    @Test
    @DisplayName("사용자 조회 성공 - username으로")
    void findByUsername_Success() {
        // given
        given(userRepository.findByUsername(testUser.getUsername()))
                .willReturn(Optional.of(testUser));

        // when
        User foundUser = userService.findByUsername(testUser.getUsername());

        // then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo(testUser.getUsername());
    }

    @Test
    @DisplayName("사용자 조회 실패 - 존재하지 않는 username")
    void findByUsername_Fail_NotFound() {
        // given
        String username = "nonexistent";
        given(userRepository.findByUsername(username)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findByUsername(username))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다: " + username);
    }

    @Test
    @DisplayName("비밀번호 확인")
    void checkPassword_Success() {
        // given
        String rawPassword = "password123";
        String encodedPassword = "encoded_password123";

        given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(true);

        // when
        boolean isMatch = userService.checkPassword(rawPassword, encodedPassword);

        // then
        assertThat(isMatch).isTrue();
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    @DisplayName("마지막 로그인 시간 업데이트")
    void updateLastLogin_Success() {
        // given
        given(userRepository.findByUsername(testUser.getUsername()))
                .willReturn(Optional.of(testUser));
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // when
        userService.updateLastLogin(testUser.getUsername());

        // then
        verify(userRepository).save(testUser);
        assertThat(testUser.getLastLogin()).isNotNull();
    }
}
