package com.board.domain.repository;

import com.board.domain.entity.Post;
import com.board.domain.entity.PostLike;
import com.board.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    /**
     * 특정 사용자가 특정 게시글에 대한 추천/비추천 기록 조회
     */
    Optional<PostLike> findByPostAndUser(Post post, User user);

    /**
     * 특정 게시글의 추천 수 조회
     */
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post = :post AND pl.isLike = true")
    Long countLikesByPost(@Param("post") Post post);

    /**
     * 특정 게시글의 비추천 수 조회
     */
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post = :post AND pl.isLike = false")
    Long countDislikesByPost(@Param("post") Post post);

    /**
     * 사용자가 특정 게시글에 추천했는지 확인
     */
    @Query("SELECT pl.isLike FROM PostLike pl WHERE pl.post = :post AND pl.user = :user")
    Optional<Boolean> findUserLikeStatus(@Param("post") Post post, @Param("user") User user);

    /**
     * 특정 게시글의 모든 추천/비추천 기록 삭제 (게시글 삭제 시 사용)
     */
    void deleteByPost(Post post);
}
