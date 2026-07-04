package ru.practicum.common.apiContracts;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.common.dto.comments.CommentStatus;

import java.util.List;
import java.util.Map;

public interface CommentsApiContract {
    @GetMapping("/api/comments/counts")
    Map<Long, Long> getCommentCountsByEventIds(@RequestParam List<Long> eventIds, @RequestParam CommentStatus status);
}
