package com.board.domain.enums;

import lombok.Getter;

/**
 * 사용자 권한 enum
 */
@Getter
public enum Role {
    USER("일반 사용자"),
    ADMIN("관리자");

    private final String description;

    Role(String description) {
        this.description = description;
    }
}
