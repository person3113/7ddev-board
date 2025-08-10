package com.board.util;

import com.board.domain.entity.User;
import jakarta.servlet.http.HttpSession;

/**
 * 세션 관련 유틸리티 클래스
 */
public class SessionUtil {

    private static final String CURRENT_USER_KEY = "currentUser";

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
     */
    public static User getCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute(CURRENT_USER_KEY);
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
