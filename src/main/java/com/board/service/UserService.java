package com.board.service;

import com.board.domain.entity.User;
import com.board.domain.enums.Role;
import com.board.domain.repository.UserRepository;
import com.board.exception.DuplicateResourceException;
import com.board.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     */
    @Transactional
    public User registerUser(String username, String email, String password, String nickname) {
        // 중복 검증
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("이미 존재하는 username입니다: " + username);
        }

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("이미 존재하는 email입니다: " + email);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);

        // 사용자 생성
        User user = User.builder()
                .username(username)
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .role(Role.USER)
                .build();

        return userRepository.save(user);
    }

    /**
     * username으로 사용자 조회
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + username));
    }

    /**
     * 비밀번호 확인
     */
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 마지막 로그인 시간 업데이트
     */
    @Transactional
    public void updateLastLogin(String username) {
        User user = findByUsername(username);
        user.updateLastLogin();
        userRepository.save(user);
    }

    /**
     * 사용자 인증 (로그인)
     */
    public User authenticateUser(String username, String password) {
        try {
            User user = findByUsername(username);
            if (checkPassword(password, user.getPassword())) {
                return user;
            }
            return null;
        } catch (ResourceNotFoundException e) {
            return null;
        }
    }
}
