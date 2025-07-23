package com.board.domain.repository;

import com.board.domain.entity.User;
import com.board.domain.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * username으로 사용자 조회
     */
    Optional<User> findByUsername(String username);

    /**
     * email로 사용자 조회
     */
    Optional<User> findByEmail(String email);

    /**
     * username 또는 email로 사용자 조회
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * username 존재 여부 확인
     */
    boolean existsByUsername(String username);

    /**
     * email 존재 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * 역할별 사용자 조회
     */
    List<User> findByRole(Role role);

    /**
     * 닉네임으로 사용자 검색 (부분 일치)
     */
    List<User> findByNicknameContaining(String nickname);
}
