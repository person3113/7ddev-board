package com.board.service;

import com.board.domain.entity.Comment;
import com.board.domain.entity.Post;
import com.board.domain.entity.User;
import com.board.domain.enums.Role;
import com.board.domain.repository.CommentRepository;
import com.board.domain.repository.PostRepository;
import com.board.domain.repository.UserRepository;
import com.board.exception.ResourceNotFoundException;
import com.board.exception.UnauthorizedException;
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

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminServiceTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    private User adminUser;
    private User normalUser;
    private Post testPost;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        // 관리자 사용자 생성
        adminUser = User.builder()
                .username("admin")
                .email("admin@example.com")
                .password("password123")
                .nickname("관리자")
                .role(Role.ADMIN)
                .build();
        adminUser = userRepository.save(adminUser);

        // 일반 사용자 생성
        normalUser = User.builder()
                .username("user")
                .email("user@example.com")
                .password("password123")
                .nickname("일반사용자")
                .role(Role.USER)
                .build();
        normalUser = userRepository.save(normalUser);

        // 테스트 게시글 생성
        testPost = Post.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .category("테스트")
                .author(normalUser)
                .build();
        testPost = postRepository.save(testPost);

        // 테스트 댓글 생성
        testComment = Comment.builder()
                .content("테스트 댓글")
                .post(testPost)
                .author(normalUser)
                .build();
        testComment = commentRepository.save(testComment);
    }

    @Test
    @DisplayName("관리자는 게시글을 강제 삭제할 수 있다")
    void adminCanForceDeletePost() {
        // when
        adminService.forceDeletePost(testPost.getId(), adminUser.getUsername());

        // then
        Post deletedPost = postRepository.findById(testPost.getId()).orElseThrow();
        assertThat(deletedPost.getDeleted()).isTrue();
    }

    @Test
    @DisplayName("일반 사용자는 게시글을 강제 삭제할 수 없다")
    void normalUserCannotForceDeletePost() {
        // when & then
        assertThatThrownBy(() -> adminService.forceDeletePost(testPost.getId(), normalUser.getUsername()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("관리자 권한이 필요합니다");
    }

    @Test
    @DisplayName("관리자는 댓글을 강제 삭제할 수 있다")
    void adminCanForceDeleteComment() {
        // when
        adminService.forceDeleteComment(testComment.getId(), adminUser.getUsername());

        // then
        Comment deletedComment = commentRepository.findById(testComment.getId()).orElseThrow();
        assertThat(deletedComment.getDeleted()).isTrue();
    }

    @Test
    @DisplayName("일반 사용자는 댓글을 강제 삭제할 수 없다")
    void normalUserCannotForceDeleteComment() {
        // when & then
        assertThatThrownBy(() -> adminService.forceDeleteComment(testComment.getId(), normalUser.getUsername()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("관리자 권한이 필요합니다");
    }

    @Test
    @DisplayName("관리자는 사용자 권한을 변경할 수 있다")
    void adminCanChangeUserRole() {
        // when
        adminService.changeUserRole(normalUser.getUsername(), Role.ADMIN, adminUser.getUsername());

        // then
        User updatedUser = userRepository.findByUsername(normalUser.getUsername()).orElseThrow();
        assertThat(updatedUser.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("일반 사용자는 사용자 권한을 변경할 수 없다")
    void normalUserCannotChangeUserRole() {
        // when & then
        assertThatThrownBy(() -> adminService.changeUserRole(normalUser.getUsername(), Role.ADMIN, normalUser.getUsername()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("관리자 권한이 필요합니다");
    }

    @Test
    @DisplayName("관리자는 모든 게시글을 조회할 수 있다")
    void adminCanViewAllPosts() {
        // given
        testPost.delete(); // 삭제된 게시글도 포함
        postRepository.save(testPost);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Post> allPosts = adminService.getAllPosts(adminUser.getUsername(), pageable);

        // then
        assertThat(allPosts.getContent()).hasSize(1);
        assertThat(allPosts.getContent().get(0).getDeleted()).isTrue(); // 삭제된 게시글도 포함
    }

    @Test
    @DisplayName("일반 사용자는 모든 게시글을 조회할 수 없다")
    void normalUserCannotViewAllPosts() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when & then
        assertThatThrownBy(() -> adminService.getAllPosts(normalUser.getUsername(), pageable))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("관리자 권한이 필요합니다");
    }

    @Test
    @DisplayName("관리자는 모든 댓글을 조회할 수 있다")
    void adminCanViewAllComments() {
        // given
        testComment.delete(); // 삭제된 댓글도 포함
        commentRepository.save(testComment);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Comment> allComments = adminService.getAllComments(adminUser.getUsername(), pageable);

        // then
        assertThat(allComments.getContent()).hasSize(1);
        assertThat(allComments.getContent().get(0).getDeleted()).isTrue(); // 삭제된 댓글도 포함
    }

    @Test
    @DisplayName("존재하지 않는 게시글 삭제 시 예외가 발생한다")
    void forceDeleteNonExistentPost_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> adminService.forceDeletePost(999L, adminUser.getUsername()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("존재하지 않는 댓글 삭제 시 예외가 발생한다")
    void forceDeleteNonExistentComment_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> adminService.forceDeleteComment(999L, adminUser.getUsername()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("댓글을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("존재하지 않는 사용자 권한 변경 시 예외가 발생한다")
    void changeNonExistentUserRole_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> adminService.changeUserRole("nonexistent", Role.ADMIN, adminUser.getUsername()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }
}
