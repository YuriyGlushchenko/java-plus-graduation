package ru.practicum.ewm.comments.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comments.dto.CommentFullDto;
import ru.practicum.ewm.comments.dto.UpdateCommentAdminRequest;
import ru.practicum.ewm.comments.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Validated
public class AdminCommentController {

    private final CommentService commentService;

    @GetMapping
    public List<CommentFullDto> getCommentsForModeration(
            @RequestParam(defaultValue = "0") @Min(0) int from,
            @RequestParam(defaultValue = "10") @Min(1) int size
    ) {
        log.debug("Admin request for get comments for moderation: from={}, size={}", from, size);
        return commentService.getCommentsForModeration(from, size);
    }

    @PatchMapping("/{userId}/{commentId}")
    public CommentFullDto moderateComment(
            @PathVariable Long userId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentAdminRequest dto
    ) {
        log.debug("Admin request for moderate comment: moderatorId={}, commentId={}", userId, commentId);
        return commentService.moderateComment(userId, commentId, dto);
    }

    @DeleteMapping("/{userId}/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable Long userId,
            @PathVariable Long commentId
    ) {
        log.debug("Admin request for delete comment: moderatorId={}, commentId={}", userId, commentId);
        commentService.deleteCommentByAdmin(userId, commentId);
    }
}