package com.board.config;

import com.board.domain.entity.User;
import com.board.domain.enums.Role;
import com.board.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 시작 시 초기 데이터를 생성하는 컴포넌트
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createAdminUser();
    }

    /**
     * 관리자 계정이 없으면 생성
     */
    private void createAdminUser() {
        String adminUsername = "admin";

        if (!userRepository.existsByUsername(adminUsername)) {
            User adminUser = User.builder()
                    .username(adminUsername)
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123!"))
                    .nickname("관리자")
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(adminUser);
            log.info("관리자 계정이 생성되었습니다. Username: {}", adminUsername);
        } else {
            log.info("관리자 계정이 이미 존재합니다. Username: {}", adminUsername);
        }
    }
}
