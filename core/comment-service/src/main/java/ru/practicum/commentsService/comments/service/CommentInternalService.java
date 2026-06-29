package ru.practicum.commentsService.comments.service;

import ru.practicum.common.dto.comments.CommentStatus;

import java.util.List;
import java.util.Map;

public interface CommentInternalService {

    Map<Long, Long> getCommentCountsByEventIds(List<Long> eventIds, CommentStatus status);
}
