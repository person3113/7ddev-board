package com.board.domain.entity;

import com.board.domain.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users",
       indexes = {@Index(name = "idx_username", columnList = "username")})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@ToString(exclude = {"password", "posts", "comments"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 마지막 로그인 시간 추가 (실무에서 중요한 필드)
    private LocalDateTime lastLogin;

    // 연관관계 (양방향)
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    @Builder
    public User(String username, String email, String password, String nickname, Role role) {
        validateUsername(username);
        validateEmail(email);
        validatePassword(password);
        validateNickname(nickname);

        this.username = username;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role != null ? role : Role.USER;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        // lastLogin은 회원가입 시에는 null (첫 로그인 시 설정)
    }

    public void updateProfile(String nickname, String email) {
        validateNickname(nickname);
        validateEmail(email);

        this.nickname = nickname;
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    public void changePassword(String newPassword) {
        validatePassword(newPassword);
        this.password = newPassword;
        this.updatedAt = LocalDateTime.now();
    }

    public void changeRole(Role role) {
        this.role = role;
        this.updatedAt = LocalDateTime.now();
    }

    // 로그인 시간 업데이트 (실무에서 필수)
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 비즈니스 메서드
    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    public int getPostCount() {
        return this.posts.size();
    }

    public int getCommentCount() {
        return this.comments.size();
    }

    // 휴면 계정 체크 (30일 미로그인)
    public boolean isDormant() {
        if (lastLogin == null) return false; // 한 번도 로그인하지 않은 경우
        return lastLogin.isBefore(LocalDateTime.now().minusDays(30));
    }

    // 신규 가입자 체크 (7일 이내)
    public boolean isNewUser() {
        return createdAt.isAfter(LocalDateTime.now().minusDays(7));
    }

    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username은 필수입니다");
        }
        if (username.length() > 50) {
            throw new IllegalArgumentException("Username은 50자를 초과할 수 없습니다");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email은 필수입니다");
        }
        if (email.length() > 100) {
            throw new IllegalArgumentException("Email은 100자를 초과할 수 없습니다");
        }
        // 간단한 이메일 형식 검증
        if (!email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password는 필수입니다");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password는 최소 6자 이상이어야 합니다");
        }
    }

    private void validateNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("Nickname은 필수입니다");
        }
        if (nickname.length() > 50) {
            throw new IllegalArgumentException("Nickname은 50자를 초과할 수 없습니다");
        }
    }
}
