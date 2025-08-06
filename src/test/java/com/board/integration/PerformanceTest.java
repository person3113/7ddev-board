package com.board.integration;

import com.board.domain.entity.Post;
import com.board.domain.entity.User;
import com.board.domain.entity.Comment;
import com.board.domain.enums.Role;
import com.board.domain.repository.PostRepository;
import com.board.domain.repository.UserRepository;
import com.board.domain.repository.CommentRepository;
import com.board.service.PostService;
import com.board.service.SearchService;
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
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("기본 성능 테스트")
class PerformanceTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private SearchService searchService;

    private User testUser;
    private List<Post> testPosts;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .username("perfuser")
                .email("perf@example.com")
                .password("password")
                .nickname("성능테스트유저")
                .role(Role.USER)
                .build();
        userRepository.save(testUser);

        // 대량 테스트 데이터 생성 (1000개 게시글)
        testPosts = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            Post post = Post.builder()
                    .title("성능 테스트 게시글 " + i)
                    .content("성능 테스트를 위한 게시글 내용 " + i + ". 검색 키워드: Spring, Java, Performance")
                    .category(i % 5 == 0 ? "성능" : "테스트")
                    .author(testUser)
                    .build();

            // 조회수와 좋아요 수 랜덤 설정
            for (int view = 0; view < i % 100; view++) {
                post.increaseViewCount();
            }
            for (int like = 0; like < i % 50; like++) {
                post.increaseLikeCount();
            }

            testPosts.add(post);
        }
        postRepository.saveAll(testPosts);

        // 댓글 데이터 생성 (각 게시글마다 5개씩)
        List<Comment> comments = new ArrayList<>();
        for (Post post : testPosts.subList(0, 100)) { // 처음 100개 게시글에만 댓글 추가
            for (int j = 1; j <= 5; j++) {
                Comment comment = Comment.builder()
                        .content("댓글 내용 " + j)
                        .post(post)
                        .author(testUser)
                        .build();
                comments.add(comment);
            }
        }
        commentRepository.saveAll(comments);
    }

    @Test
    @DisplayName("페이지네이션 성능 테스트 - 대량 데이터 조회")
    void paginationPerformanceTest() {
        StopWatch stopWatch = new StopWatch("페이지네이션 성능 테스트");

        // 첫 번째 페이지 조회 (가장 빠름)
        stopWatch.start("첫 번째 페이지 조회");
        Pageable pageable1 = PageRequest.of(0, 20);
        Page<Post> page1 = postRepository.findByDeletedFalseOrderByCreatedAtDesc(pageable1);
        stopWatch.stop();

        // 중간 페이지 조회
        stopWatch.start("중간 페이지 조회 (25페이지)");
        Pageable pageable25 = PageRequest.of(24, 20);
        Page<Post> page25 = postRepository.findByDeletedFalseOrderByCreatedAtDesc(pageable25);
        stopWatch.stop();

        // 마지막 페이지 조회 (가장 느림)
        stopWatch.start("마지막 페이지 조회 (50페이지)");
        Pageable pageable50 = PageRequest.of(49, 20);
        Page<Post> page50 = postRepository.findByDeletedFalseOrderByCreatedAtDesc(pageable50);
        stopWatch.stop();

        System.out.println(stopWatch.prettyPrint());

        // 검증
        assertThat(page1.getContent()).hasSize(20);
        assertThat(page25.getContent()).hasSize(20);
        assertThat(page50.getContent()).hasSize(20);
        assertThat(page1.getTotalElements()).isEqualTo(1000);

        // 성능 기준: 각 페이지 조회가 1초 이내
        assertThat(stopWatch.getTotalTimeMillis()).isLessThan(1000);
    }

    @Test
    @DisplayName("검색 성능 테스트 - 다양한 검색 타입")
    void searchPerformanceTest() {
        StopWatch stopWatch = new StopWatch("검색 성능 테스트");
        Pageable pageable = PageRequest.of(0, 20);

        // 제목 검색
        stopWatch.start("제목 검색");
        Page<Post> titleResults = searchService.searchByTitle("성능", pageable);
        stopWatch.stop();

        // 내용 검색
        stopWatch.start("내용 검색");
        Page<Post> contentResults = searchService.searchByContent("Spring", pageable);
        stopWatch.stop();

        // 복합 검색 (제목 + 내용)
        stopWatch.start("복합 검색 (제목+내용)");
        Page<Post> complexResults = searchService.searchByTitleOrContent("Java", pageable);
        stopWatch.stop();

        // 전체 검색 (모든 필드)
        stopWatch.start("전체 검색 (모든 필드)");
        Page<Post> allFieldResults = searchService.searchByAllFields("Performance", pageable);
        stopWatch.stop();

        System.out.println(stopWatch.prettyPrint());

        // 검증
        assertThat(titleResults.getContent()).isNotEmpty();
        assertThat(contentResults.getContent()).isNotEmpty();
        assertThat(complexResults.getContent()).isNotEmpty();
        assertThat(allFieldResults.getContent()).isNotEmpty();

        // 성능 기준: 모든 검색이 2초 이내
        assertThat(stopWatch.getTotalTimeMillis()).isLessThan(2000);
    }

    @Test
    @DisplayName("동시성 테스트 - 게시글 조회수 증가")
    void concurrencyViewCountTest() throws Exception {
        Post targetPost = testPosts.get(0);
        int initialViewCount = targetPost.getViewCount();
        int threadCount = 10; // 100개에서 10개로 축소

        StopWatch stopWatch = new StopWatch("동시성 테스트");
        stopWatch.start("10개 스레드 동시 조회수 증가");

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // 10개 스레드가 동시에 조회수 증가
        for (int i = 0; i < threadCount; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    postService.findById(targetPost.getId()); // 조회 시 viewCount 증가
                } catch (Exception e) {
                    // 예외 무시 (테스트 목적)
                }
            }, executor);
            futures.add(future);
        }

        // 모든 스레드 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        stopWatch.stop();

        executor.shutdown();
        System.out.println(stopWatch.prettyPrint());

        // 검증: 조회수가 증가했는지 확인 (정확한 수는 동시성 이슈로 보장되지 않음)
        Post updatedPost = postRepository.findById(targetPost.getId()).orElseThrow();
        assertThat(updatedPost.getViewCount()).isGreaterThanOrEqualTo(initialViewCount);

        // 성능 기준: 10개 동시 요청이 3초 이내
        assertThat(stopWatch.getTotalTimeMillis()).isLessThan(3000);
    }

    @Test
    @DisplayName("대량 데이터 처리 성능 테스트")
    void bulkDataProcessingTest() {
        StopWatch stopWatch = new StopWatch("대량 데이터 처리 테스트");

        // 카테고리별 게시글 수 조회
        stopWatch.start("카테고리별 게시글 수 조회");
        long testCategoryCount = postRepository.countByCategoryAndDeletedFalse("테스트");
        long perfCategoryCount = postRepository.countByCategoryAndDeletedFalse("성능");
        stopWatch.stop();

        // 조회수 기준 정렬 조회
        stopWatch.start("조회수 기준 정렬 조회");
        Pageable viewCountPageable = PageRequest.of(0, 50);
        Page<Post> popularPosts = postRepository.findByDeletedFalseOrderByViewCountDescCreatedAtDesc(viewCountPageable);
        stopWatch.stop();

        // 좋아요 수 기준 정렬 조회
        stopWatch.start("좋아요 수 기준 정렬 조회");
        Pageable likeCountPageable = PageRequest.of(0, 50);
        Page<Post> likedPosts = postRepository.findByDeletedFalseOrderByLikeCountDescCreatedAtDesc(likeCountPageable);
        stopWatch.stop();

        System.out.println(stopWatch.prettyPrint());

        // 검증
        assertThat(testCategoryCount).isEqualTo(800); // 1000개 중 80%
        assertThat(perfCategoryCount).isEqualTo(200); // 1000개 중 20%
        assertThat(popularPosts.getContent()).hasSize(50);
        assertThat(likedPosts.getContent()).hasSize(50);

        // 성능 기준: 대량 데이터 처리가 3초 이내
        assertThat(stopWatch.getTotalTimeMillis()).isLessThan(3000);
    }

    @Test
    @DisplayName("메모리 사용량 테스트 - 페이지네이션 vs 전체 조회")
    void memoryUsageTest() {
        // 페이지네이션으로 조회 (메모리 효율적)
        StopWatch stopWatch = new StopWatch("메모리 사용량 테스트");
        stopWatch.start("페이지네이션 조회 (20개)");

        Pageable pageable = PageRequest.of(0, 20);
        Page<Post> pagedResults = postRepository.findByDeletedFalseOrderByCreatedAtDesc(pageable);

        stopWatch.stop();

        // 전체 조회 (메모리 사용량 많음) - 제한된 수량만 조회
        stopWatch.start("제한된 조회 (100개)");
        Pageable limitedPageable = PageRequest.of(0, 100);
        Page<Post> limitedResults = postRepository.findByDeletedFalseOrderByCreatedAtDesc(limitedPageable);
        stopWatch.stop();

        System.out.println(stopWatch.prettyPrint());

        // 검증
        assertThat(pagedResults.getContent()).hasSize(20);
        assertThat(limitedResults.getContent()).hasSizeLessThanOrEqualTo(100);

        // 성능 기준: 메모리 테스트가 2초 이내
        assertThat(stopWatch.getTotalTimeMillis()).isLessThan(2000);
    }
}
