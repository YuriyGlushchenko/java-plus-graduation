package ru.practicum.ewm.comments.controller;


import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comments.dto.CommentShortDto;
import ru.practicum.ewm.comments.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
@Validated
public class PublicCommentController {

    private final CommentService commentService;

    @GetMapping("/events/{eventId}/comments")
    public List<CommentShortDto> getEventComments(
            @PathVariable long eventId,
            @RequestParam(defaultValue = "0") @Min(0) int from,
            @RequestParam(defaultValue = "10") @Min(1) int size
    ) {
        log.debug("Public request for get comments for event: eventId={}, from={}, size={}", eventId, from, size);
        return commentService.getEventComments(eventId, from, size);
    }

    @GetMapping("/comments/{commentId}")
    public CommentShortDto getComment(@PathVariable Long commentId) {
        log.debug("Public request for get comment by id: {}", commentId);
        return commentService.getComment(commentId);
    }
}