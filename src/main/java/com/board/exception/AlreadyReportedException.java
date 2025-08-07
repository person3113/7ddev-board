package com.board.exception;

/**
 * 이미 신고한 내용에 대해 중복 신고를 시도할 때 발생하는 예외
 */
public class AlreadyReportedException extends RuntimeException {

    public AlreadyReportedException(String message) {
        super(message);
    }

    public AlreadyReportedException(String message, Throwable cause) {
        super(message, cause);
    }
}
