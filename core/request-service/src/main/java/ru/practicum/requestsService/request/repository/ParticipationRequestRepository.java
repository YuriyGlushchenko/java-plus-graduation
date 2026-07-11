package ru.practicum.requestsService.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.common.dto.participationRequest.RequestStatus;
import ru.practicum.requestsService.request.model.ParticipationRequest;

import java.util.Collection;
import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    boolean existsByEventIdAndRequesterId(Long eventId, Long userId);

    List<ParticipationRequest> findByRequesterId(Long userId);

    List<ParticipationRequest> findByEventId(Long eventId);

    List<ParticipationRequest> findByIdIn(List<Long> requestId);

    @Modifying
    @Query("UPDATE ParticipationRequest r SET r.status = :status WHERE r.id IN :ids")
    int updateStatusByIdIn(@Param("ids") List<Long> ids, @Param("status") RequestStatus status);

    @Modifying
    @Query("UPDATE ParticipationRequest r SET r.status = :newStatus WHERE r.eventId = :eventId AND r.status = :oldStatus")
    int updateStatusByEventId(
            @Param("eventId") Long eventId,
            @Param("oldStatus") RequestStatus oldStatus,
            @Param("newStatus") RequestStatus newStatus
    );

    @Query("SELECT r.eventId, COUNT(r) FROM ParticipationRequest r " +
            "WHERE r.eventId IN :eventIds AND r.status = :status " +
            "GROUP BY r.eventId")
    List<Object[]> countByEventIdInAndStatus(
            @Param("eventIds") Collection<Long> eventIds,
            @Param("status") RequestStatus status
    );


    boolean existsByEventIdAndRequesterIdAndStatus(Long eventId,
                                                   Long requesterId,
                                                   RequestStatus status);

}
