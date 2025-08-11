package com.board.service;

import com.board.domain.entity.Post;
import com.board.domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final PostRepository postRepository;

    /**
     * 제목으로 검색 (성능 최적화)
     */
    public Page<Post> searchByTitle(String keyword, Pageable pageable) {
        if (!StringUtils.hasText(keyword)) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 검색어 전처리 (앞뒤 공백 제거, 특수문자 처리)
        String processedKeyword = preprocessKeyword(keyword);
        if (!StringUtils.hasText(processedKeyword)) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        log.debug("제목 검색 실행: keyword={}, page={}, size={}", processedKeyword, pageable.getPageNumber(), pageable.getPageSize());
        return postRepository.findByTitleContainingIgnoreCaseAndDeletedFalse(processedKeyword, pageable);
    }

    /**
     * 내용으로 검색 (성능 최적화)
     */
    public Page<Post> searchByContent(String keyword, Pageable pageable) {
        if (!StringUtils.hasText(keyword)) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        String processedKeyword = preprocessKeyword(keyword);
        if (!StringUtils.hasText(processedKeyword)) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        log.debug("내용 검색 실행: keyword={}, page={}, size={}", processedKeyword, pageable.getPageNumber(), pageable.getPageSize());
        return postRepository.findByContentContainingIgnoreCaseAndDeletedFalse(processedKeyword, pageable);
    }

    /**
     * 작성자 닉네임으로 검색
     */
    public Page<Post> searchByAuthor(String authorName, Pageable pageable) {
        if (!StringUtils.hasText(authorName)) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        String processedAuthorName = preprocessKeyword(authorName);
        if (!StringUtils.hasText(processedAuthorName)) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        log.debug("작성자 검색 실행: authorName={}, page={}, size={}", processedAuthorName, pageable.getPageNumber(), pageable.getPageSize());
        return postRepository.findByAuthorNicknameContainingAndDeletedFalse(processedAuthorName, pageable);
    }

    /**
     * 카테고리로 검색
     */
    public Page<Post> searchByCategory(String category, Pageable pageable) {
        if (!StringUtils.hasText(category)) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        log.debug("카테고리 검색 실행: category={}, page={}, size={}", category, pageable.getPageNumber(), pageable.getPageSize());
        return postRepository.findByCategoryAndDeletedFalseOrderByCreatedAtDesc(category, pageable);
    }

    /**
     * 제목 또는 내용에서 키워드 검색 (복합 검색, 성능 최적화)
     */
    public Page<Post> searchByTitleOrContent(String keyword, Pageable pageable) {
        if (!StringUtils.hasText(keyword)) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        String processedKeyword = preprocessKeyword(keyword);
        if (!StringUtils.hasText(processedKeyword)) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        log.debug("제목+내용 검색 실행: keyword={}, page={}, size={}", processedKeyword, pageable.getPageNumber(), pageable.getPageSize());
        return postRepository.findByTitleOrContentContainingIgnoreCaseAndDeletedFalse(processedKeyword, pageable);
    }

    /**
     * 모든 필드에서 키워드 검색 (댓글 내용 포함, 성능 최적화)
     */
    public Page<Post> searchByAllFields(String keyword, Pageable pageable) {
        if (!StringUtils.hasText(keyword)) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        String processedKeyword = preprocessKeyword(keyword);
        if (!StringUtils.hasText(processedKeyword)) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        log.debug("전체 필드 검색 실행 (댓글 포함): keyword={}, page={}, size={}", processedKeyword, pageable.getPageNumber(), pageable.getPageSize());
        return postRepository.findByAllFieldsContainingIgnoreCase(processedKeyword, pageable);
    }

    /**
     * 카테고리와 키워드로 복합 검색
     */
    public Page<Post> searchByCategoryAndKeyword(String category, String keyword, Pageable pageable) {
        if (!StringUtils.hasText(category) || !StringUtils.hasText(keyword)) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        return postRepository.findByCategoryAndKeyword(category, keyword, pageable);
    }

    /**
     * 작성자와 키워드로 복합 검색
     */
    public Page<Post> searchByAuthorAndKeyword(String authorName, String keyword, Pageable pageable) {
        if (!StringUtils.hasText(authorName) || !StringUtils.hasText(keyword)) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        return postRepository.findByAuthorAndKeyword(authorName, keyword, pageable);
    }

    /**
     * 통합 검색 (검색 타입에 따른 분기 처리, 성능 최적화)
     */
    public Page<Post> search(String searchType, String keyword, String category, String author, Pageable pageable) {
        if (!StringUtils.hasText(keyword)) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 검색 실행 시간 측정 시작
        long startTime = System.currentTimeMillis();

        Page<Post> result = switch (searchType) {
            case "title" -> searchByTitle(keyword, pageable);
            case "content" -> searchByContent(keyword, pageable);
            case "author" -> searchByAuthor(keyword, pageable);
            case "category" -> searchByCategory(keyword, pageable);
            case "comment" -> searchByComments(keyword, pageable);
            case "title_content" -> searchByTitleOrContent(keyword, pageable);
            case "all" -> searchByAllFields(keyword, pageable);
            default -> searchByTitleOrContent(keyword, pageable); // 기본값
        };

        // 검색 실행 시간 로깅
        long executionTime = System.currentTimeMillis() - startTime;
        log.info("검색 실행 완료: type={}, keyword={}, resultCount={}, executionTime={}",
                searchType, keyword, result.getTotalElements(), executionTime);

        return result;
    }

    /**
     * 검색어 전처리 (성능 최적화)
     */
    private String preprocessKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return "";
        }

        // 앞뒤 공백 제거
        String processed = keyword.trim();

        // 연속된 공백을 하나로 변경
        processed = processed.replaceAll("\\s+", " ");

        // 특수문자 이스케이프 (SQL Injection 방지)
        processed = processed.replaceAll("[%_]", "\\\\$0");

        // 최소 검색어 길이 체크 (1자 이상)
        if (processed.length() < 1) {
            return "";
        }

        // 최대 검색어 길이 체크 (100자 이하)
        if (processed.length() > 100) {
            processed = processed.substring(0, 100);
        }

        return processed;
    }

    /**
     * 검색 결과 카운트 조회 (성능 최적화)
     */
    public Long getSearchResultCount(String searchType, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return 0L;
        }

        String processedKeyword = preprocessKeyword(keyword);
        if (!StringUtils.hasText(processedKeyword)) {
            return 0L;
        }

        return switch (searchType) {
            case "title" -> postRepository.countByTitleContainingIgnoreCaseAndDeletedFalse(processedKeyword);
            case "content" -> postRepository.countByContentContainingIgnoreCaseAndDeletedFalse(processedKeyword);
            case "comment" -> postRepository.countByCommentsContentContainingIgnoreCaseAndDeletedFalse(processedKeyword);
            default -> 0L;
        };
    }

    /**
     * 댓글 내용으로 검색 (성능 최적화)
     */
    public Page<Post> searchByComments(String keyword, Pageable pageable) {
        if (!StringUtils.hasText(keyword)) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        String processedKeyword = preprocessKeyword(keyword);
        if (!StringUtils.hasText(processedKeyword)) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        log.debug("댓글 내용 검색 실행: keyword={}, page={}, size={}", processedKeyword, pageable.getPageNumber(), pageable.getPageSize());
        return postRepository.findByCommentsContentContainingIgnoreCaseAndDeletedFalse(processedKeyword, pageable);
    }
}
