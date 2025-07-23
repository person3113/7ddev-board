package com.board.domain.repository;

import com.board.domain.entity.Post;
import com.board.domain.entity.User;
import com.board.domain.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("PostRepository 통합 테스트")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private User author;

    @BeforeEach
    void setUp() {
        author = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .nickname("테스트유저")
                .role(Role.USER)
                .build();
        userRepository.save(author);
    }

    @Test
    @DisplayName("삭제되지 않은 게시글만 조회할 수 있다")
    void findByDeletedFalse() {
        // given
        Post post1 = Post.builder()
                .title("첫 번째 게시글")
                .content("첫 번째 내용")
                .category("자유게시판")
                .author(author)
                .build();

        Post post2 = Post.builder()
                .title("두 번째 게시글")
                .content("두 번째 내용")
                .category("자유게시판")
                .author(author)
                .build();

        postRepository.save(post1);
        postRepository.save(post2);

        // 하나는 삭제
        post2.delete();
        postRepository.save(post2);

        // when
        Page<Post> posts = postRepository.findByDeletedFalseOrderByCreatedAtDesc(PageRequest.of(0, 10));

        // then
        assertThat(posts.getContent()).hasSize(1);
        assertThat(posts.getContent().get(0).getTitle()).isEqualTo("첫 번째 게시글");
        assertThat(posts.getContent().get(0).isDeleted()).isFalse();
    }

    @Test
    @DisplayName("카테고리별로 게시글을 조회할 수 있다")
    void findByCategory() {
        // given
        Post post1 = Post.builder()
                .title("자유게시판 글")
                .content("자유게시판 내용")
                .category("자유게시판")
                .author(author)
                .build();

        Post post2 = Post.builder()
                .title("공지사항 글")
                .content("공지사항 내용")
                .category("공지사항")
                .author(author)
                .build();

        postRepository.save(post1);
        postRepository.save(post2);

        // when
        Page<Post> freePosts = postRepository.findByCategoryAndDeletedFalseOrderByCreatedAtDesc(
                "자유게시판", PageRequest.of(0, 10));

        // then
        assertThat(freePosts.getContent()).hasSize(1);
        assertThat(freePosts.getContent().get(0).getCategory()).isEqualTo("자유게시판");
    }

    @Test
    @DisplayName("제목으로 게시글을 검색할 수 있다")
    void findByTitleContaining() {
        // given
        Post post1 = Post.builder()
                .title("Spring Boot 강좌")
                .content("스프링 부트 내용")
                .category("기술")
                .author(author)
                .build();

        Post post2 = Post.builder()
                .title("Java 기초")
                .content("자바 기초 내용")
                .category("기술")
                .author(author)
                .build();

        postRepository.save(post1);
        postRepository.save(post2);

        // when
        Page<Post> posts = postRepository.findByTitleContainingAndDeletedFalseOrderByCreatedAtDesc("Spring", PageRequest.of(0, 10));

        // then
        assertThat(posts.getContent()).hasSize(1);
        assertThat(posts.getContent().get(0).getTitle()).contains("Spring");
    }

    @Test
    @DisplayName("카테고리 목록을 조회할 수 있다")
    void findDistinctCategories() {
        // given
        Post post1 = Post.builder()
                .title("자유게시판 글1")
                .content("내용1")
                .category("자유게시판")
                .author(author)
                .build();

        Post post2 = Post.builder()
                .title("자유게시판 글2")
                .content("내용2")
                .category("자유게시판")
                .author(author)
                .build();

        Post post3 = Post.builder()
                .title("공지사항 글")
                .content("공지내용")
                .category("공지사항")
                .author(author)
                .build();

        postRepository.save(post1);
        postRepository.save(post2);
        postRepository.save(post3);

        // when
        List<String> categories = postRepository.findDistinctCategories();

        // then
        assertThat(categories).hasSize(2);
        assertThat(categories).containsExactlyInAnyOrder("자유게시판", "공지사항");
    }

    @Test
    @DisplayName("전체 게시글 수를 조회할 수 있다")
    void countByDeletedFalse() {
        // given
        Post post1 = Post.builder()
                .title("게시글1")
                .content("내용1")
                .category("자유게시판")
                .author(author)
                .build();

        Post post2 = Post.builder()
                .title("게시글2")
                .content("내용2")
                .category("자유게시판")
                .author(author)
                .build();

        postRepository.save(post1);
        postRepository.save(post2);

        // 하나 삭제
        post2.delete();
        postRepository.save(post2);

        // when
        long count = postRepository.countByDeletedFalse();

        // then
        assertThat(count).isEqualTo(1);
    }
}
