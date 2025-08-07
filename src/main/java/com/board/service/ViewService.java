package com.board.service;

import com.board.domain.entity.Post;
import com.board.domain.repository.PostRepository;
import com.board.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ViewService {

    private final PostRepository postRepository;

    private static final String VIEW_SESSION_PREFIX = "viewed_post_";

    /**
     * 게시글 조회수 증가 (중복 조회 방지)
     * 세션 기반으로 중복 조회를 방지합니다.
     */
    public void increaseViewCount(Long postId, HttpServletRequest request) {
        log.debug("조회수 증가 시도: postId={}", postId);

        Post post = findPostById(postId);

        // 세션에서 이미 조회한 게시글인지 확인
        if (!hasViewedPost(postId, request)) {
            post.increaseViewCount();
            markAsViewed(postId, request);
            log.debug("조회수 증가 완료: postId={}, newViewCount={}", postId, post.getViewCount());
        } else {
            log.debug("이미 조회한 게시글입니다: postId={}", postId);
        }
    }

    /**
     * 특정 게시글을 이미 조회했는지 확인
     */
    private boolean hasViewedPost(Long postId, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        String sessionKey = VIEW_SESSION_PREFIX + postId;
        return session.getAttribute(sessionKey) != null;
    }

    /**
     * 게시글을 조회한 것으로 세션에 기록
     */
    private void markAsViewed(Long postId, HttpServletRequest request) {
        HttpSession session = request.getSession(true); // 세션이 없으면 생성
        String sessionKey = VIEW_SESSION_PREFIX + postId;
        session.setAttribute(sessionKey, System.currentTimeMillis());

        // 세션 타임아웃 설정 (30분)
        session.setMaxInactiveInterval(30 * 60);
    }

    /**
     * 게시글의 현재 조회수 조회
     */
    @Transactional(readOnly = true)
    public Integer getViewCount(Long postId) {
        Post post = findPostById(postId);
        return post.getViewCount();
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다: " + postId));
    }
}
