package ru.practicum.commentsService.comments.dto;

import lombok.RequiredArgsConstructor;
import ru.practicum.commentsService.comments.model.Comment;
import ru.practicum.commentsService.comments.model.CommentStatus;
import ru.practicum.common.dto.comments.CommentFullDto;
import ru.practicum.common.dto.comments.CommentShortDto;
import ru.practicum.userService.user.dto.UserMapper;
import ru.practicum.userService.user.model.User;


import java.time.LocalDateTime;


@RequiredArgsConstructor
public class CommentMapper {

    public static Comment toComment(NewCommentDto dto, User author, Long event) {
        return Comment.builder()
                .text(dto.getText())
                .author(author)
                .eventId(event)
                .created(LocalDateTime.now())
                .status(CommentStatus.PENDING)
                .build();
    }

    public static void updateCommentFromUserRequest(UpdateCommentUserRequest request, Comment comment) {
        boolean updated = false;
        if (request.getText() != null) {
            comment.setText(request.getText());
            updated = true;
        }
        if (request.getStatus() != null) {
            comment.setStatus(CommentStatus.DELETED); // в сервисе уже проверили, что статус если пришел в запросе, то только DELETE
            updated = true;
        }
        if (updated) {
            comment.setUpdated(LocalDateTime.now());
        }
    }

    public static void updateCommentFromAdminRequest(UpdateCommentAdminRequest request, Comment comment, User moderator) {
        boolean updated = false; // флаг, был ли изменен коммент в итоге или только модератор поменял статус
        if (request.getText() != null) {
            comment.setText(request.getText());
            updated = true;
        }
        if (request.getStatus() != null) {  // модерация, это же не обновление по сути, а этап жизненного цикла, по этому updated не меняем
            comment.setStatus(request.getStatus());
            comment.setModerator(moderator);
            comment.setModeratedAt(LocalDateTime.now());
        }
        if (updated) {
            comment.setUpdated(LocalDateTime.now());
        }
    }

    public static CommentFullDto toFullDto(Comment comment) {
        CommentFullDto.CommentFullDtoBuilder builder = CommentFullDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .author(UserMapper.toUserShortDto(comment.getAuthor()))
                .eventId(comment.getEventId())
                .created(comment.getCreated())
                .updated(comment.getUpdated())
                .status(comment.getStatus().name());

        if (comment.getModerator() != null) {
            builder.moderator(UserMapper.toUserShortDto(comment.getModerator()))
                    .moderatedAt(comment.getModeratedAt());
        }

        return builder.build();
    }

    public static CommentShortDto toShortDto(Comment comment) {
        return CommentShortDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .author(UserMapper.toUserShortDto(comment.getAuthor()))
                .eventId(comment.getEventId())
                .created(comment.getCreated())
                .build();
    }

    public static void adminDeleteComment(Comment comment, User moderator) {
        comment.setStatus(CommentStatus.DELETED);
        comment.setModerator(moderator);
        comment.setModeratedAt(LocalDateTime.now());
        comment.setUpdated(LocalDateTime.now());
    }
}