package com.board.domain.repository;

import com.board.domain.entity.User;
import com.board.domain.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository 통합 테스트")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자를 저장하고 조회할 수 있다")
    void saveAndFindUser() {
        // given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .nickname("테스트유저")
                .role(Role.USER)
                .build();

        // when
        User savedUser = userRepository.save(user);
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("username으로 사용자를 조회할 수 있다")
    void findByUsername() {
        // given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .nickname("테스트유저")
                .role(Role.USER)
                .build();
        userRepository.save(user);

        // when
        Optional<User> foundUser = userRepository.findByUsername("testuser");

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("email로 사용자를 조회할 수 있다")
    void findByEmail() {
        // given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .nickname("테스트유저")
                .role(Role.USER)
                .build();
        userRepository.save(user);

        // when
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("username 중복을 확인할 수 있다")
    void existsByUsername() {
        // given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .nickname("테스트유저")
                .role(Role.USER)
                .build();
        userRepository.save(user);

        // when & then
        assertThat(userRepository.existsByUsername("testuser")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }

    @Test
    @DisplayName("역할별로 사용자를 조회할 수 있다")
    void findByRole() {
        // given
        User user1 = User.builder()
                .username("user1")
                .email("user1@example.com")
                .password("password123")
                .nickname("일반유저1")
                .role(Role.USER)
                .build();

        User admin = User.builder()
                .username("admin")
                .email("admin@example.com")
                .password("password123")
                .nickname("관리자")
                .role(Role.ADMIN)
                .build();

        userRepository.save(user1);
        userRepository.save(admin);

        // when
        List<User> users = userRepository.findByRole(Role.USER);
        List<User> admins = userRepository.findByRole(Role.ADMIN);

        // then
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getRole()).isEqualTo(Role.USER);
        assertThat(admins).hasSize(1);
        assertThat(admins.get(0).getRole()).isEqualTo(Role.ADMIN);
    }
}
