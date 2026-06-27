package ru.practicum.commentsService.comments.dto;

import lombok.RequiredArgsConstructor;
import ru.practicum.commentsService.comments.model.Comment;
import ru.practicum.common.dto.comments.CommentStatus;
import ru.practicum.common.dto.comments.CommentFullDto;
import ru.practicum.common.dto.comments.CommentShortDto;
import ru.practicum.common.dto.users.UserShortDto;

import java.time.LocalDateTime;


@RequiredArgsConstructor
public class CommentMapper {

    public static Comment toComment(NewCommentDto dto, Long author, Long event) {
        return Comment.builder()
                .text(dto.getText())
                .authorId(author)
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

    public static void updateCommentFromAdminRequest(UpdateCommentAdminRequest request, Comment comment, Long moderatorId) {
        boolean updated = false; // флаг, был ли изменен коммент в итоге или только модератор поменял статус
        if (request.getText() != null) {
            comment.setText(request.getText());
            comment.setModeratorId(moderatorId);
            comment.setModeratedAt(LocalDateTime.now());
            updated = true;
        }
        if (request.getStatus() != null) {  // модерация, это же не обновление по сути, а этап жизненного цикла комментраия, по этому updated не меняем
            comment.setStatus(request.getStatus());
            comment.setModeratorId(moderatorId);
            comment.setModeratedAt(LocalDateTime.now());
        }
        if (updated) {
            comment.setUpdated(LocalDateTime.now());
        }
    }

    public static CommentFullDto toFullDto(Comment comment, UserShortDto user, UserShortDto moderator) {
        CommentFullDto.CommentFullDtoBuilder builder = CommentFullDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .author(user)
                .eventId(comment.getEventId())
                .created(comment.getCreated())
                .updated(comment.getUpdated())
                .status(comment.getStatus().name());

        if (comment.getModeratorId() != null && moderator != null) {
            builder.moderator(moderator)
                    .moderatedAt(comment.getModeratedAt());
        }

        return builder.build();
    }

    public static CommentShortDto toShortDto(Comment comment, UserShortDto user) {
        return CommentShortDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .author(user)
                .eventId(comment.getEventId())
                .created(comment.getCreated())
                .build();
    }

    public static void adminDeleteComment(Comment comment, UserShortDto moderator) {
        comment.setStatus(CommentStatus.DELETED);
        comment.setModeratorId(moderator.getId());
        comment.setModeratedAt(LocalDateTime.now());
        comment.setUpdated(LocalDateTime.now());
    }
}