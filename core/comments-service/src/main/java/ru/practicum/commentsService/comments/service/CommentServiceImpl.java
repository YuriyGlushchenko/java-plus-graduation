package ru.practicum.commentsService.comments.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.commentsService.comments.client.EventClient;
import ru.practicum.commentsService.comments.client.UserClient;
import ru.practicum.commentsService.comments.dto.CommentMapper;
import ru.practicum.commentsService.comments.dto.NewCommentDto;
import ru.practicum.commentsService.comments.dto.UpdateCommentAdminRequest;
import ru.practicum.commentsService.comments.dto.UpdateCommentUserRequest;
import ru.practicum.commentsService.comments.model.Comment;
import ru.practicum.commentsService.comments.repository.CommentRepository;
import ru.practicum.common.dto.comments.CommentFullDto;
import ru.practicum.common.dto.comments.CommentShortDto;
import ru.practicum.common.dto.comments.CommentStatus;
import ru.practicum.common.dto.events.EventBaseDto;
import ru.practicum.common.dto.events.EventState;
import ru.practicum.common.dto.users.UserShortDto;
import ru.practicum.common.exceptions.exceptions.ConditionsNotMetException;
import ru.practicum.common.exceptions.exceptions.NotFoundException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserClient userClient;
    private final EventClient eventClient;

    @Override
    public List<CommentShortDto> getEventComments(Long eventId, int from, int size) {
        PageRequest page = PageRequest.of(from / size, size);

        List<Comment> comments = commentRepository.findByEventIdAndStatusOrderByCreatedDesc(
                eventId, CommentStatus.APPROVED, page);  // публичный запрос, по этому только APPROVED


        if (comments.isEmpty()) {
            return Collections.emptyList();
        }

        // собираем id всех авторов
        List<Long> userIds = comments.stream().map(Comment::getAuthorId).toList();

        // загружаем одним запросом всех юзеров по их id
        Map<Long, UserShortDto> userMap;
        try {
            userMap = userClient.getUsersShortByIds(userIds);
            if (userMap == null) {
                throw new NotFoundException("Comments authors not found");
            }
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new NotFoundException("Comments authors not found");
            } else {
                log.error("User service unavailable: status={}, error={}", e.status(), e.getMessage());
                throw new RuntimeException("User service is currently unavailable", e);
            }
        }

        return comments.stream()
                .map(comment -> {
                    UserShortDto author = userMap.get(comment.getAuthorId());
                    UserShortDto moderator = comment.getModeratorId() != null
                            ? userMap.get(comment.getModeratorId())
                            : null;

                    return CommentMapper.toShortDto(comment, author);
                })
                .collect(Collectors.toList());


    }

    @Override
    public CommentShortDto getComment(Long commentId) {
        Comment comment = commentRepository.findByIdAndStatus(commentId, CommentStatus.APPROVED)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " not found or not approved"));
        return CommentMapper.toShortDto(comment);
    }

    @Override
    @Transactional
    public CommentFullDto createComment(Long userId, Long eventId, NewCommentDto dto) {

        UserShortDto author; // получаем пользователя - автора создаваемого комментария
        try {
            author = userClient.getUserShortById(userId);
            if (author == null) {
                throw new NotFoundException("User with id=" + userId + " not found");
            }
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new NotFoundException("User with id=" + userId + " was not found");
            } else {
                log.error("User service unavailable: status={}, error={}", e.status(), e.getMessage());
                throw new RuntimeException("User service is currently unavailable", e);
            }
        }

        EventBaseDto event; // получаем событие для которого создается комментарий
        try {
            event = eventClient.getBaseEventInfo(eventId);
            if (event == null) {
                throw new NotFoundException("Event with id=" + eventId + " not found");
            }
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new NotFoundException("Event with id=" + eventId + " was not found");
            } else {
                log.error("Event service unavailable: status={}, error={}", e.status(), e.getMessage());
                throw new RuntimeException("Event service is currently unavailable", e);
            }
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConditionsNotMetException("Cannot comment on unpublished event");
        }

        Comment comment = CommentMapper.toComment(dto, author.getId(), eventId);
        comment = commentRepository.save(comment);
        return CommentMapper.toFullDto(comment, author, null);
    }

    @Override
    @Transactional
    public CommentFullDto updateCommentByUser(Long userId, Long commentId, UpdateCommentUserRequest dto) {
        // пользователь может менять статус только на DELETE
        if (dto.getStatus() != null && dto.getStatus() != CommentStatus.DELETED) {
            throw new ConditionsNotMetException("User can only set status to DELETED");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " not found"));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConditionsNotMetException("Only author or admin can update comments");
        }

        // если меняем текст у коммента в статусе кроме PENDING, тогда ошибкО
        if (dto.getText() != null && comment.getStatus() != CommentStatus.PENDING) {
            throw new ConditionsNotMetException("Text can only be changed when comment is in PENDING status");
        }

        CommentMapper.updateCommentFromUserRequest(dto, comment);
        comment = commentRepository.save(comment);
        return CommentMapper.toFullDto(comment);
    }

    @Override
    @Transactional
    public void deleteCommentByUser(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " not found"));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConditionsNotMetException("Only author or admin can delete comments");
        }

        comment.setStatus(CommentStatus.DELETED);
        comment.setUpdated(LocalDateTime.now());
        commentRepository.save(comment);
    }

    @Override
    public List<CommentFullDto> getCommentsForModeration(int from, int size) {
        PageRequest page = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findByStatusOrderByCreatedAsc(CommentStatus.PENDING, page);
        return comments.stream()
                .map(CommentMapper::toFullDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentFullDto moderateComment(Long moderatorId, Long commentId, UpdateCommentAdminRequest dto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " not found"));

        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new NotFoundException("Moderator with id=" + moderatorId + " not found"));

        CommentMapper.updateCommentFromAdminRequest(dto, comment, moderator);
        comment = commentRepository.save(comment);
        return CommentMapper.toFullDto(comment);
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(Long moderatorId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " not found"));

        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new NotFoundException("Moderator with id=" + moderatorId + " not found"));

        CommentMapper.adminDeleteComment(comment, moderator);
        commentRepository.save(comment);
    }
}