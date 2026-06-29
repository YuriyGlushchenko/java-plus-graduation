package ru.practicum.commentsService.comments.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.common.dto.comments.CommentStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String text;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private LocalDateTime created;

    private LocalDateTime updated;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentStatus status;

    @Column(name = "moderated_by")
    private Long moderatorId;

    private LocalDateTime moderatedAt;
}