package com.board.service;

import com.board.domain.entity.Post;
import com.board.domain.entity.User;
import com.board.domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Post 도메인 서비스
 *
 * 게시글의 생성, 조회, 수정, 삭제 등의 핵심 비즈니스 로직을 담당합니다.
 * TDD 방식으로 구현되었으며, 도메인 규칙을 엄격하게 준수합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;

    /**
     * 게시글 생성
     *
     * @param title 게시글 제목 (필수, 200자 이하)
     * @param content 게시글 내용 (필수)
     * @param category 카테고리 (선택)
     * @param author 작성자 (필수)
     * @return 생성된 게시글
     * @throws IllegalArgumentException 필수 필드가 누락되거나 유효하지 않은 경우
     */
    public Post createPost(String title, String content, String category, User author) {
        log.debug("게시글 생성 요청 - 제목: {}, 작성자: {}", title, author != null ? author.getUsername() : "null");

        Post post = Post.builder()
                .title(title)
                .content(content)
                .category(category)
                .author(author)
                .build();

        Post savedPost = postRepository.save(post);
        log.info("게시글 생성 완료 - ID: {}, 제목: {}", savedPost.getId(), savedPost.getTitle());

        return savedPost;
    }

    /**
     * 게시글 조회 (ID)
     *
     * @param id 게시글 ID
     * @return 조회된 게시글
     * @throws IllegalArgumentException 게시글이 존재하지 않거나 삭제된 경우
     */
    @Transactional(readOnly = true)
    public Post findById(Long id) {
        log.debug("게시글 조회 요청 - ID: {}", id);

        Post post = postRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("게시글 조회 실패 - 존재하지 않는 ID: {}", id);
                    return new IllegalArgumentException("게시글을 찾을 수 없습니다. ID: " + id);
                });

        if (post.isDeleted()) {
            log.warn("게시글 조회 실패 - 삭제된 게시글 ID: {}", id);
            throw new IllegalArgumentException("삭제된 게시글입니다. ID: " + id);
        }

        log.debug("게시글 조회 성공 - ID: {}, 제목: {}", post.getId(), post.getTitle());
        return post;
    }

    /**
     * 게시글 수정
     *
     * @param id 수정할 게시글 ID
     * @param title 새로운 제목
     * @param content 새로운 내용
     * @param category 새로운 카테고리
     * @param requestUser 수정 요청자
     * @return 수정된 게시글
     * @throws IllegalArgumentException 권한이 없거나 게시글이 존재하지 않는 경우
     */
    public Post updatePost(Long id, String title, String content, String category, User requestUser) {
        log.debug("게시글 수정 요청 - ID: {}, 요청자: {}", id, requestUser.getUsername());

        Post post = findById(id);

        if (!post.canEdit(requestUser)) {
            log.warn("게시글 수정 권한 없음 - 게시글 ID: {}, 요청자: {}, 작성자: {}",
                    id, requestUser.getUsername(), post.getAuthor().getUsername());
            throw new IllegalArgumentException("게시글을 수정할 권한이 없습니다");
        }

        post.update(title, content, category);
        Post updatedPost = postRepository.save(post);

        log.info("게시글 수정 완료 - ID: {}, 제목: {}", updatedPost.getId(), updatedPost.getTitle());
        return updatedPost;
    }

    /**
     * 게시글 삭제 (소프트 삭제)
     *
     * @param id 삭제할 게시글 ID
     * @param requestUser 삭제 요청자
     * @throws IllegalArgumentException 권한이 없거나 게시글이 존재하지 않는 경우
     */
    public void deletePost(Long id, User requestUser) {
        log.debug("게시글 삭제 요청 - ID: {}, 요청자: {}", id, requestUser.getUsername());

        Post post = findById(id);

        if (!post.canDelete(requestUser)) {
            log.warn("게시글 삭제 권한 없음 - 게시글 ID: {}, 요청자: {}, 작성자: {}",
                    id, requestUser.getUsername(), post.getAuthor().getUsername());
            throw new IllegalArgumentException("게시글을 삭제할 권한이 없습니다");
        }

        post.delete();
        postRepository.save(post);

        log.info("게시글 삭제 완료 (소프트 삭제) - ID: {}, 제목: {}", post.getId(), post.getTitle());
    }

    /**
     * 게시글 조회수 증가
     *
     * @param id 게시글 ID
     * @return 조회수가 증가된 게시글
     */
    public Post increaseViewCount(Long id) {
        log.debug("게시글 조회수 증가 요청 - ID: {}", id);

        Post post = findById(id);
        post.increaseViewCount();
        Post updatedPost = postRepository.save(post);

        log.debug("게시글 조회수 증가 완료 - ID: {}, 현재 조회수: {}", id, updatedPost.getViewCount());
        return updatedPost;
    }

    /**
     * 게시글 목록 조회 (페이지네이션)
     *
     * @param pageable 페이지 정보
     * @return 게시글 목록 (삭제되지 않은 게시글만)
     */
    @Transactional(readOnly = true)
    public Page<Post> findAll(Pageable pageable) {
        log.debug("게시글 목록 조회 요청 - 페이지: {}, 크기: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Post> posts = postRepository.findByDeletedFalseOrderByCreatedAtDesc(pageable);

        log.debug("게시글 목록 조회 완료 - 전체: {}, 현재 페이지 게시글 수: {}",
                posts.getTotalElements(), posts.getNumberOfElements());

        return posts;
    }
}
