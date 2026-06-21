package ru.practicum.ewm.comments.service;


import ru.practicum.ewm.comments.dto.*;

import java.util.List;

public interface CommentService {

    List<CommentShortDto> getEventComments(Long eventId, int from, int size);

    CommentShortDto getComment(Long commentId);

    CommentFullDto createComment(Long userId, Long eventId, NewCommentDto dto);

    CommentFullDto updateCommentByUser(Long userId, Long commentId, UpdateCommentUserRequest dto);

    void deleteCommentByUser(Long userId, Long commentId);

    List<CommentFullDto> getCommentsForModeration(int from, int size);

    CommentFullDto moderateComment(Long moderatorId, Long commentId, UpdateCommentAdminRequest dto);

    void deleteCommentByAdmin(Long moderatorId, Long commentId);
}