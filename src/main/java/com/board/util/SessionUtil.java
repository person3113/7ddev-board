package com.board.util;

import com.board.domain.entity.User;
import com.board.domain.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 세션 관련 유틸리티 클래스
 */
@Component
public class SessionUtil {

    private static final String CURRENT_USER_KEY = "currentUser";

    private static UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        SessionUtil.userRepository = userRepository;
    }

    /**
     * 세션에서 현재 로그인한 사용자 정보를 가져옵니다.
     * 로그인하지 않은 경우 예외를 발생시킵니다.
     */
    public static Long getCurrentUserId(HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser != null) {
            return currentUser.getId();
        }
        // 로그인하지 않은 경우 예외 발생 (보안 강화)
        throw new IllegalStateException("로그인이 필요합니다.");
    }

    /**
     * 세션에서 현재 로그인한 사용자 정보를 가져옵니다.
     * 로그인하지 않은 경우에도 안전하게 처리할 수 있도록 nullable 반환
     */
    public static Long getCurrentUserIdSafe(HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser != null) {
            return currentUser.getId();
        }
        // 임시: 로그인하지 않은 경우 관리자 ID 반환 (나중에 로그인 강제로 변경 예정)
        return 1L;
    }

    /**
     * 세션에서 현재 로그인한 사용자 정보를 가져옵니다.
     * 세션에 없으면 Spring Security에서 가져옵니다.
     */
    public static User getCurrentUser(HttpSession session) {
        if (session == null) {
            return getUserFromSecurity();
        }

        // 먼저 세션에서 확인
        User sessionUser = (User) session.getAttribute(CURRENT_USER_KEY);
        if (sessionUser != null) {
            return sessionUser;
        }

        // 세션에 없으면 Spring Security에서 가져와서 세션에 저장
        User securityUser = getUserFromSecurity();
        if (securityUser != null && session != null) {
            session.setAttribute(CURRENT_USER_KEY, securityUser);
        }

        return securityUser;
    }

    /**
     * Spring Security에서 현재 인증된 사용자 정보를 가져옵니다.
     */
    private static User getUserFromSecurity() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())) {

                String username = authentication.getName();
                if (userRepository != null) {
                    return userRepository.findByUsername(username).orElse(null);
                }
            }
        } catch (Exception e) {
            // Security Context에서 사용자 정보를 가져올 수 없는 경우
        }
        return null;
    }

    /**
     * 현재 사용자가 로그인되어 있는지 확인합니다.
     */
    public static boolean isLoggedIn(HttpSession session) {
        return getCurrentUser(session) != null;
    }

    /**
     * 세션에 사용자 정보를 저장합니다.
     */
    public static void setCurrentUser(HttpSession session, User user) {
        if (session != null) {
            session.setAttribute(CURRENT_USER_KEY, user);
        }
    }

    /**
     * 세션에서 사용자 정보를 제거합니다.
     */
    public static void removeCurrentUser(HttpSession session) {
        if (session != null) {
            session.removeAttribute(CURRENT_USER_KEY);
        }
    }
}
