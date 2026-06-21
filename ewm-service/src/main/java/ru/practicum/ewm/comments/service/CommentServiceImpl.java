package ru.practicum.ewm.comments.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comments.dto.*;
import ru.practicum.ewm.comments.model.Comment;
import ru.practicum.ewm.comments.model.CommentStatus;
import ru.practicum.ewm.comments.repository.CommentRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exceptions.exceptions.ConditionsNotMetException;
import ru.practicum.ewm.exceptions.exceptions.NotFoundException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<CommentShortDto> getEventComments(Long eventId, int from, int size) {
        PageRequest page = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findByEventIdAndStatusOrderByCreatedDesc(
                eventId, CommentStatus.APPROVED, page);  // публичный запрос, по этому только APPROVED
        return comments.stream()
                .map(CommentMapper::toShortDto)
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
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConditionsNotMetException("Cannot comment on unpublished event");
        }

        Comment comment = CommentMapper.toComment(dto, author, event);
        comment = commentRepository.save(comment);
        return CommentMapper.toFullDto(comment);
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