package com.board.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * 전역 예외 처리 핸들러
 *
 * 애플리케이션 전반에서 발생하는 예외를 일관성 있게 처리합니다.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 404 Not Found 예외 처리
     */
    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatusException(ResponseStatusException e, Model model,
                                              jakarta.servlet.http.HttpServletResponse response) {
        log.warn("ResponseStatusException 발생: {} {}", e.getStatusCode(), e.getMessage());

        // HTTP 상태 코드 설정
        response.setStatus(e.getStatusCode().value());

        if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            model.addAttribute("error", "요청하신 페이지를 찾을 수 없습니다.");
            model.addAttribute("message", e.getReason());
            return "error/404";
        }

        if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
            model.addAttribute("error", "접근 권한이 없습니다.");
            model.addAttribute("message", e.getReason());
            return "error/403";
        }

        // 기타 HTTP 상태 예외
        model.addAttribute("error", "오류가 발생했습니다.");
        model.addAttribute("message", e.getReason());
        return "error/error";
    }

    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgumentException(IllegalArgumentException e, Model model) {
        log.warn("IllegalArgumentException 발생: {}", e.getMessage());

        model.addAttribute("error", "잘못된 요청입니다.");
        model.addAttribute("message", e.getMessage());
        return "error/400";
    }

    /**
     * 일반적인 예외 처리
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception e, Model model) {
        log.error("예상하지 못한 오류 발생", e);

        model.addAttribute("error", "서버 내부 오류가 발생했습니다.");
        model.addAttribute("message", "잠시 후 다시 시도해주세요.");
        return "error/500";
    }
}
