package ru.practicum.ewm.comments.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comments.dto.CommentFullDto;
import ru.practicum.ewm.comments.dto.NewCommentDto;
import ru.practicum.ewm.comments.dto.UpdateCommentUserRequest;
import ru.practicum.ewm.comments.service.CommentService;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
@Validated
public class PrivateCommentController {

    private final CommentService commentService;

    @PostMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentFullDto createComment(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody NewCommentDto dto
    ) {
        log.debug("Private request for creating comment: userId={}, eventId={}", userId, eventId);
        return commentService.createComment(userId, eventId, dto);
    }

    @PatchMapping("/{commentId}")
    public CommentFullDto updateComment(
            @PathVariable Long userId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentUserRequest dto
    ) {
        log.debug("Private request for update comment: userId={}, commentId={}", userId, commentId);
        return commentService.updateCommentByUser(userId, commentId, dto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable Long userId,
            @PathVariable Long commentId
    ) {
        log.debug("Private request for deleting comment: userId={}, commentId={}", userId, commentId);
        commentService.deleteCommentByUser(userId, commentId);
    }
}