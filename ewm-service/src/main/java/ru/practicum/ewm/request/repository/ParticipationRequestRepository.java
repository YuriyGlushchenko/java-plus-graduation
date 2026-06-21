package ru.practicum.ewm.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.model.RequestStatus;

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
    @Query("UPDATE ParticipationRequest r SET r.status = :status WHERE r.event.id = :id AND r.status = :oldStatus")
    int updateStatusByEventId(@Param("id") Long id,
                              @Param("oldStatus") RequestStatus oldStatus,
                              @Param("status") RequestStatus newStatus);

}
