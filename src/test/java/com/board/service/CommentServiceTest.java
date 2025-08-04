package com.board.service;

import com.board.domain.entity.Comment;
import com.board.domain.entity.Post;
import com.board.domain.entity.User;
import com.board.domain.enums.Role;
import com.board.domain.repository.CommentRepository;
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
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private User author;
    private User otherUser;
    private Post post;

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

        otherUser = User.builder()
                .username("otheruser")
                .email("other@example.com")
                .password("password123")
                .nickname("다른유저")
                .role(Role.USER)
                .build();
        userRepository.save(otherUser);

        // 테스트 게시글 생성
        post = Post.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .category("테스트")
                .author(author)
                .build();
        postRepository.save(post);
    }

    @Test
    @DisplayName("댓글 생성 - 성공")
    void createComment_Success() {
        // given
        String content = "테스트 댓글 내용";

        // when
        Comment comment = commentService.createComment(post.getId(), content, author);

        // then
        assertThat(comment).isNotNull();
        assertThat(comment.getContent()).isEqualTo(content);
        assertThat(comment.getPost()).isEqualTo(post);
        assertThat(comment.getAuthor()).isEqualTo(author);
        assertThat(comment.getParent()).isNull();
        assertThat(comment.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("대댓글 생성 - 성공")
    void createReply_Success() {
        // given
        Comment parentComment = Comment.builder()
                .content("부모 댓글")
                .post(post)
                .author(author)
                .build();
        commentRepository.save(parentComment);

        String replyContent = "대댓글 내용";

        // when
        Comment reply = commentService.createReply(parentComment.getId(), replyContent, otherUser);

        // then
        assertThat(reply).isNotNull();
        assertThat(reply.getContent()).isEqualTo(replyContent);
        assertThat(reply.getPost()).isEqualTo(post);
        assertThat(reply.getAuthor()).isEqualTo(otherUser);
        assertThat(reply.getParent()).isEqualTo(parentComment);
        assertThat(reply.isReply()).isTrue();
    }

    @Test
    @DisplayName("댓글 생성 - 내용이 비어있으면 실패")
    void createComment_EmptyContent_ThrowsException() {
        // given
        String emptyContent = "";

        // when & then
        assertThatThrownBy(() -> commentService.createComment(post.getId(), emptyContent, author))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("댓글 내용은 필수입니다");
    }

    @Test
    @DisplayName("댓글 생성 - 존재하지 않는 게시글에 댓글 작성 시 실패")
    void createComment_PostNotFound_ThrowsException() {
        // given
        Long nonExistentPostId = 999L;
        String content = "테스트 댓글";

        // when & then
        assertThatThrownBy(() -> commentService.createComment(nonExistentPostId, content, author))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("게시글을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("댓글 조회 - ID로 조회 성공")
    void findById_Success() {
        // given
        Comment comment = Comment.builder()
                .content("테스트 댓글")
                .post(post)
                .author(author)
                .build();
        commentRepository.save(comment);

        // when
        Comment foundComment = commentService.findById(comment.getId());

        // then
        assertThat(foundComment).isNotNull();
        assertThat(foundComment.getId()).isEqualTo(comment.getId());
        assertThat(foundComment.getContent()).isEqualTo("테스트 댓글");
    }

    @Test
    @DisplayName("댓글 조회 - 존재하지 않는 ID로 조회 시 실패")
    void findById_NotFound_ThrowsException() {
        // given
        Long nonExistentId = 999L;

        // when & then
        assertThatThrownBy(() -> commentService.findById(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("댓글을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("댓글 조회 - 삭제된 댓글 조회 시 실패")
    void findById_DeletedComment_ThrowsException() {
        // given
        Comment comment = Comment.builder()
                .content("테스트 댓글")
                .post(post)
                .author(author)
                .build();
        comment.delete();
        commentRepository.save(comment);

        // when & then
        assertThatThrownBy(() -> commentService.findById(comment.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("삭제된 댓글입니다");
    }

    @Test
    @DisplayName("게시글별 댓글 목록 조회 - 성공")
    void findCommentsByPost_Success() {
        // given
        Comment comment1 = Comment.builder()
                .content("첫 번째 댓글")
                .post(post)
                .author(author)
                .build();
        commentRepository.save(comment1);

        Comment comment2 = Comment.builder()
                .content("두 번째 댓글")
                .post(post)
                .author(otherUser)
                .build();
        commentRepository.save(comment2);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Comment> comments = commentService.findCommentsByPost(post.getId(), pageable);

        // then
        assertThat(comments).isNotNull();
        assertThat(comments.getContent()).hasSize(2);
        assertThat(comments.getContent().get(0).getContent()).isEqualTo("첫 번째 댓글");
        assertThat(comments.getContent().get(1).getContent()).isEqualTo("두 번째 댓글");
    }

    @Test
    @DisplayName("댓글 수정 - 성공")
    void updateComment_Success() {
        // given
        Comment comment = Comment.builder()
                .content("원본 댓글")
                .post(post)
                .author(author)
                .build();
        commentRepository.save(comment);

        String newContent = "수정된 댓글";

        // when
        Comment updatedComment = commentService.updateComment(comment.getId(), newContent, author);

        // then
        assertThat(updatedComment.getContent()).isEqualTo(newContent);
    }

    @Test
    @DisplayName("댓글 수정 - 작성자가 아닌 경우 실패")
    void updateComment_NotAuthor_ThrowsException() {
        // given
        Comment comment = Comment.builder()
                .content("원본 댓글")
                .post(post)
                .author(author)
                .build();
        commentRepository.save(comment);

        String newContent = "수정된 댓글";

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(comment.getId(), newContent, otherUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("댓글 수정 권한이 없습니다");
    }

    @Test
    @DisplayName("댓글 삭제 - 성공")
    void deleteComment_Success() {
        // given
        Comment comment = Comment.builder()
                .content("삭제할 댓글")
                .post(post)
                .author(author)
                .build();
        commentRepository.save(comment);

        // when
        commentService.deleteComment(comment.getId(), author);

        // then
        Comment deletedComment = commentRepository.findById(comment.getId()).orElseThrow();
        assertThat(deletedComment.isDeleted()).isTrue();
        assertThat(deletedComment.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("댓글 삭제 - 작성자가 아닌 경우 실패")
    void deleteComment_NotAuthor_ThrowsException() {
        // given
        Comment comment = Comment.builder()
                .content("삭제할 댓글")
                .post(post)
                .author(author)
                .build();
        commentRepository.save(comment);

        // when & then
        assertThatThrownBy(() -> commentService.deleteComment(comment.getId(), otherUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("댓글 삭제 권한이 없습니다");
    }
}
