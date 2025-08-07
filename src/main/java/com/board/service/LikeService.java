package com.board.service;

import com.board.domain.entity.Post;
import com.board.domain.entity.PostLike;
import com.board.domain.entity.User;
import com.board.domain.repository.PostLikeRepository;
import com.board.domain.repository.PostRepository;
import com.board.domain.repository.UserRepository;
import com.board.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LikeService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;

    /**
     * 게시글 추천
     */
    public void likePost(Long postId, Long userId) {
        log.debug("게시글 추천 시도: postId={}, userId={}", postId, userId);

        Post post = findPostById(postId);
        User user = findUserById(userId);

        Optional<PostLike> existingLike = postLikeRepository.findByPostAndUser(post, user);

        if (existingLike.isPresent()) {
            PostLike postLike = existingLike.get();
            if (postLike.getIsLike()) {
                // 이미 추천한 상태 - 아무것도 하지 않음
                log.debug("이미 추천한 게시글입니다: postId={}, userId={}", postId, userId);
                return;
            } else {
                // 비추천 -> 추천으로 변경
                postLike.updateLike(true);
                post.increaseLikeCount();
                log.debug("비추천에서 추천으로 변경: postId={}, userId={}", postId, userId);
            }
        } else {
            // 새로운 추천
            PostLike newLike = PostLike.builder()
                    .post(post)
                    .user(user)
                    .isLike(true)
                    .build();
            postLikeRepository.save(newLike);
            post.increaseLikeCount();
            log.debug("새로운 추천 생성: postId={}, userId={}", postId, userId);
        }
    }

    /**
     * 게시글 비추천
     */
    public void dislikePost(Long postId, Long userId) {
        log.debug("게시글 비추천 시도: postId={}, userId={}", postId, userId);

        Post post = findPostById(postId);
        User user = findUserById(userId);

        Optional<PostLike> existingLike = postLikeRepository.findByPostAndUser(post, user);

        if (existingLike.isPresent()) {
            PostLike postLike = existingLike.get();
            if (!postLike.getIsLike()) {
                // 이미 비추천한 상태 - 아무것도 하지 않음
                log.debug("이미 비추천한 게시글입니다: postId={}, userId={}", postId, userId);
                return;
            } else {
                // 추천 -> 비추천으로 변경
                postLike.updateLike(false);
                post.decreaseLikeCount();
                log.debug("추천에서 비추천으로 변경: postId={}, userId={}", postId, userId);
            }
        } else {
            // 새로운 비추천
            PostLike newDislike = PostLike.builder()
                    .post(post)
                    .user(user)
                    .isLike(false)
                    .build();
            postLikeRepository.save(newDislike);
            log.debug("새로운 비추천 생성: postId={}, userId={}", postId, userId);
        }
    }

    /**
     * 추천/비추천 취소
     */
    public void cancelLike(Long postId, Long userId) {
        log.debug("추천/비추천 취소 시도: postId={}, userId={}", postId, userId);

        Post post = findPostById(postId);
        User user = findUserById(userId);

        Optional<PostLike> existingLike = postLikeRepository.findByPostAndUser(post, user);

        if (existingLike.isPresent()) {
            PostLike postLike = existingLike.get();
            if (postLike.getIsLike()) {
                // 추천 취소
                post.decreaseLikeCount();
            }
            // 비추천인 경우 추천 수는 변경되지 않음

            postLikeRepository.delete(postLike);
            log.debug("추천/비추천 취소 완료: postId={}, userId={}", postId, userId);
        } else {
            log.debug("취소할 추천/비추천이 없습니다: postId={}, userId={}", postId, userId);
        }
    }

    /**
     * 사용자의 게시글 추천 상태 조회
     * @return null: 추천/비추천 없음, true: 추천, false: 비추천
     */
    @Transactional(readOnly = true)
    public Boolean getUserLikeStatus(Long postId, Long userId) {
        Post post = findPostById(postId);
        User user = findUserById(userId);

        return postLikeRepository.findUserLikeStatus(post, user).orElse(null);
    }

    /**
     * 게시글의 총 추천 수 조회
     */
    @Transactional(readOnly = true)
    public Long getLikeCount(Long postId) {
        Post post = findPostById(postId);
        return postLikeRepository.countLikesByPost(post);
    }

    /**
     * 게시글의 총 비추천 수 조회
     */
    @Transactional(readOnly = true)
    public Long getDislikeCount(Long postId) {
        Post post = findPostById(postId);
        return postLikeRepository.countDislikesByPost(post);
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다: " + postId));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));
    }
}
