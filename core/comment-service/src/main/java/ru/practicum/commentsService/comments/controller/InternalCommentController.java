package ru.practicum.commentsService.comments.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.commentsService.comments.service.CommentInternalService;
import ru.practicum.common.dto.comments.CommentStatus;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class InternalCommentController implements ru.practicum.common.apiContracts.CommentsApiContract {

    private final CommentInternalService commentInternalService;

    @GetMapping("/counts")
    @Override
    public Map<Long, Long> getCommentCountsByEventIds(@RequestParam List<Long> eventIds, @RequestParam CommentStatus status) {
        return commentInternalService.getCommentCountsByEventIds(eventIds, status);
    }
}
