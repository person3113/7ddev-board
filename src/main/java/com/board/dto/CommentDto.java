package com.board.dto;

import com.board.domain.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class CommentDto {
    private Long id;
    private String content;
    private String authorNickname;
    private Long authorId;
    private Integer likeCount;
    private Boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isReply;
    private Long parentId;
    private List<CommentDto> children;
    private Boolean likedByCurrentUser;

    public static CommentDto from(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.getDisplayContent())
                .authorNickname(comment.getAuthor().getNickname())
                .authorId(comment.getAuthor().getId())
                .likeCount(comment.getLikeCount())
                .deleted(comment.getDeleted())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .isReply(comment.isReply())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .children(comment.getChildren().stream()
                        .map(CommentDto::from)
                        .collect(Collectors.toList()))
                .likedByCurrentUser(false) // 기본값, 별도로 설정 필요
                .build();
    }

    public static CommentDto from(Comment comment, boolean likedByCurrentUser) {
        CommentDto dto = from(comment);
        return CommentDto.builder()
                .id(dto.getId())
                .content(dto.getContent())
                .authorNickname(dto.getAuthorNickname())
                .authorId(dto.getAuthorId())
                .likeCount(dto.getLikeCount())
                .deleted(dto.getDeleted())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .isReply(dto.getIsReply())
                .parentId(dto.getParentId())
                .children(dto.getChildren())
                .likedByCurrentUser(likedByCurrentUser)
                .build();
    }
}
