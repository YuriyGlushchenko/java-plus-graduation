package ru.practicum.commentsService.comments.repository;


import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.commentsService.comments.model.Comment;
import ru.practicum.commentsService.comments.model.CommentStatus;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByEventIdAndStatusOrderByCreatedDesc(Long eventId, CommentStatus status, Pageable pageable);

    Optional<Comment> findByIdAndStatus(Long id, CommentStatus status);

    List<Comment> findByStatusOrderByCreatedAsc(CommentStatus status, Pageable pageable);

    Optional<Comment> findByIdAndAuthorId(Long id, Long authorId);

    boolean existsByIdAndAuthorId(Long id, Long authorId);

    // запрос возвращает для каждого id из списка набор из двух чисел: eventId и количество комментов для этого eventId.
    // Этот набор из двух чисел представлен как массив Object. Либо, как вариант, можно создать проекцию из этих двух чисел и
    // возвращать список проекций.
@Query("SELECT c.eventId, COUNT(c) FROM Comment c WHERE c.eventId IN :eventIds AND c.status = :status GROUP BY c.eventId")
List<Object[]> countByEventIdInAndStatus(@Param("eventIds") List<Long> eventIds, @Param("status") CommentStatus status);

}