package com.board.service;

import com.board.domain.entity.Post;
import com.board.domain.entity.User;
import com.board.domain.enums.Role;
import com.board.domain.repository.PostRepository;
import com.board.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class PostPaginationTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private User author;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        author = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .nickname("테스트유저")
                .role(Role.USER)
                .build();
        userRepository.save(author);

        // 테스트 게시글들 생성 (다양한 조회수와 좋아요 수)
        for (int i = 1; i <= 25; i++) {
            Post post = Post.builder()
                    .title("테스트 게시글 " + i)
                    .content("테스트 내용 " + i)
                    .category("테스트")
                    .author(author)
                    .build();

            // 조회수와 좋아요 수를 다르게 설정
            for (int j = 0; j < i; j++) {
                post.increaseViewCount();
            }
            for (int k = 0; k < (26 - i); k++) {
                post.increaseLikeCount();
            }

            postRepository.save(post);
        }
    }

    @Test
    @DisplayName("게시글 목록 페이징 - 기본 최신순")
    void findAllWithPagination_Latest() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Post> posts = postService.findAll(pageable);

        // then
        assertThat(posts).isNotNull();
        assertThat(posts.getContent()).hasSize(10);
        assertThat(posts.getTotalElements()).isEqualTo(25);
        assertThat(posts.getTotalPages()).isEqualTo(3);
        assertThat(posts.getNumber()).isEqualTo(0);
        assertThat(posts.hasNext()).isTrue();
        assertThat(posts.hasPrevious()).isFalse();

        // 최신순 정렬 확인 (마지막에 생성된 게시글이 첫 번째)
        assertThat(posts.getContent().get(0).getTitle()).isEqualTo("테스트 게시글 25");
    }

    @Test
    @DisplayName("게시글 목록 페이징 - 조회수순 정렬")
    void findAllWithPagination_ViewCount() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Post> posts = postService.findAllWithSort(pageable, "viewCount");

        // then
        assertThat(posts).isNotNull();
        assertThat(posts.getContent()).hasSize(10);
        assertThat(posts.getTotalElements()).isEqualTo(25);

        // 조회수순 정렬 확인 (조회수가 가장 높은 게시글이 첫 번째)
        assertThat(posts.getContent().get(0).getTitle()).isEqualTo("테스트 게시글 25");
        assertThat(posts.getContent().get(0).getViewCount()).isEqualTo(25);

        // 두 번째 게시글 확인
        assertThat(posts.getContent().get(1).getTitle()).isEqualTo("테스트 게시글 24");
        assertThat(posts.getContent().get(1).getViewCount()).isEqualTo(24);
    }

    @Test
    @DisplayName("게시글 목록 페이징 - 좋아요수순 정렬")
    void findAllWithPagination_LikeCount() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Post> posts = postService.findAllWithSort(pageable, "likeCount");

        // then
        assertThat(posts).isNotNull();
        assertThat(posts.getContent()).hasSize(10);
        assertThat(posts.getTotalElements()).isEqualTo(25);

        // 좋아요순 정렬 확인 (좋아요가 가장 높은 게시글이 첫 번째)
        assertThat(posts.getContent().get(0).getTitle()).isEqualTo("테스트 게시글 1");
        assertThat(posts.getContent().get(0).getLikeCount()).isEqualTo(25);

        // 두 번째 게시글 확인
        assertThat(posts.getContent().get(1).getTitle()).isEqualTo("테스트 게시글 2");
        assertThat(posts.getContent().get(1).getLikeCount()).isEqualTo(24);
    }

    @Test
    @DisplayName("게시글 목록 페이징 - 두 번째 페이지")
    void findAllWithPagination_SecondPage() {
        // given
        Pageable pageable = PageRequest.of(1, 10);

        // when
        Page<Post> posts = postService.findAll(pageable);

        // then
        assertThat(posts).isNotNull();
        assertThat(posts.getContent()).hasSize(10);
        assertThat(posts.getTotalElements()).isEqualTo(25);
        assertThat(posts.getTotalPages()).isEqualTo(3);
        assertThat(posts.getNumber()).isEqualTo(1);
        assertThat(posts.hasNext()).isTrue();
        assertThat(posts.hasPrevious()).isTrue();
    }

    @Test
    @DisplayName("게시글 목록 페이징 - 마지막 페이지")
    void findAllWithPagination_LastPage() {
        // given
        Pageable pageable = PageRequest.of(2, 10);

        // when
        Page<Post> posts = postService.findAll(pageable);

        // then
        assertThat(posts).isNotNull();
        assertThat(posts.getContent()).hasSize(5); // 마지막 페이지는 5개
        assertThat(posts.getTotalElements()).isEqualTo(25);
        assertThat(posts.getTotalPages()).isEqualTo(3);
        assertThat(posts.getNumber()).isEqualTo(2);
        assertThat(posts.hasNext()).isFalse();
        assertThat(posts.hasPrevious()).isTrue();
        assertThat(posts.isLast()).isTrue();
    }

    @Test
    @DisplayName("게시글 목록 페이징 - 빈 페이지")
    void findAllWithPagination_EmptyPage() {
        // given
        postRepository.deleteAll(); // 모든 게시글 삭제
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Post> posts = postService.findAll(pageable);

        // then
        assertThat(posts).isNotNull();
        assertThat(posts.getContent()).isEmpty();
        assertThat(posts.getTotalElements()).isEqualTo(0);
        assertThat(posts.getTotalPages()).isEqualTo(0);
        assertThat(posts.getNumber()).isEqualTo(0);
        assertThat(posts.hasNext()).isFalse();
        assertThat(posts.hasPrevious()).isFalse();
    }

    @Test
    @DisplayName("페이지 크기 변경 테스트")
    void findAllWithPagination_DifferentPageSize() {
        // given
        Pageable pageable = PageRequest.of(0, 5);

        // when
        Page<Post> posts = postService.findAll(pageable);

        // then
        assertThat(posts).isNotNull();
        assertThat(posts.getContent()).hasSize(5);
        assertThat(posts.getTotalElements()).isEqualTo(25);
        assertThat(posts.getTotalPages()).isEqualTo(5); // 페이지 크기가 5이므로 총 5페이지
        assertThat(posts.getSize()).isEqualTo(5);
    }

    @Test
    @DisplayName("잘못된 정렬 옵션 - 기본값(최신순)으로 처리")
    void findAllWithPagination_InvalidSortOption() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Post> posts = postService.findAllWithSort(pageable, "invalidSort");

        // then
        assertThat(posts).isNotNull();
        assertThat(posts.getContent()).hasSize(10);

        // 기본값(최신순)으로 정렬되어야 함
        assertThat(posts.getContent().get(0).getTitle()).isEqualTo("테스트 게시글 25");
    }
}
