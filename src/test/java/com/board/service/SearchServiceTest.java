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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SearchServiceTest {

    @Autowired
    private SearchService searchService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Post post1, post2, post3, post4;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .nickname("테스트유저")
                .role(Role.USER)
                .build();
        userRepository.save(testUser);

        // 테스트 게시글들 생성
        post1 = Post.builder()
                .title("Spring Boot 튜토리얼")
                .content("Spring Boot를 이용한 웹 개발 방법")
                .category("개발")
                .author(testUser)
                .build();

        post2 = Post.builder()
                .title("Java 기초 문법")
                .content("자바 프로그래밍 언어의 기본 문법을 배워보자")
                .category("프로그래밍")
                .author(testUser)
                .build();

        post3 = Post.builder()
                .title("데이터베이스 설계")
                .content("효율적인 DB 설계 방법론")
                .category("개발")
                .author(testUser)
                .build();

        post4 = Post.builder()
                .title("React 컴포넌트")
                .content("React를 이용한 컴포넌트 개발")
                .category("프론트엔드")
                .author(testUser)
                .build();

        postRepository.save(post1);
        postRepository.save(post2);
        postRepository.save(post3);
        postRepository.save(post4);
    }

    @Test
    @DisplayName("제목으로 검색 - 성공")
    void searchByTitle_Success() {
        // given
        String keyword = "Spring";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Post> result = searchService.searchByTitle(keyword, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).contains("Spring");
    }

    @Test
    @DisplayName("내용으로 검색 - 성공")
    void searchByContent_Success() {
        // given
        String keyword = "자바";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Post> result = searchService.searchByContent(keyword, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getContent()).contains("자바");
    }

    @Test
    @DisplayName("작성자로 검색 - 성공")
    void searchByAuthor_Success() {
        // given
        String authorName = "테스트유저";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Post> result = searchService.searchByAuthor(authorName, pageable);

        // then
        assertThat(result.getContent()).hasSize(4);
        assertThat(result.getContent()).allMatch(post ->
            post.getAuthor().getNickname().equals(authorName));
    }

    @Test
    @DisplayName("카테고리로 검색 - 성공")
    void searchByCategory_Success() {
        // given
        String category = "개발";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Post> result = searchService.searchByCategory(category, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(post ->
            post.getCategory().equals(category));
    }

    @Test
    @DisplayName("복합 검색 - 제목과 내용에서 키워드 검색")
    void searchByTitleOrContent_Success() {
        // given
        String keyword = "Java";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Post> result = searchService.searchByTitleOrContent(keyword, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).contains("Java");
    }

    @Test
    @DisplayName("복합 검색 - 모든 필드에서 키워드 검색")
    void searchByAllFields_Success() {
        // given
        String keyword = "개발";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Post> result = searchService.searchByAllFields(keyword, pageable);

        // then
        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("빈 키워드로 검색 시 빈 결과 반환")
    void searchWithEmptyKeyword_ReturnsEmpty() {
        // given
        String keyword = "";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Post> result = searchService.searchByTitle(keyword, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("null 키워드로 검색 시 빈 결과 반환")
    void searchWithNullKeyword_ReturnsEmpty() {
        // given
        String keyword = null;
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Post> result = searchService.searchByTitle(keyword, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 키워드로 검색 시 빈 결과 반환")
    void searchWithNonExistentKeyword_ReturnsEmpty() {
        // given
        String keyword = "존재하지않는키워드";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Post> result = searchService.searchByTitle(keyword, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
    }
}
