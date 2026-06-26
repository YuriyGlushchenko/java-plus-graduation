package ru.practicum.eventsService.event.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.common.dto.events.EventState;
import ru.practicum.common.dto.events.Location;
import ru.practicum.eventsService.categories.model.Category;
import ru.practicum.userService.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Builder
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)  //  безопасно для @OneToMany и lazy-загрузок
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA обязан иметь конструктор без аргументов
@AllArgsConstructor  // для Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String annotation;

    @Column(nullable = false, length = 7000)
    private String description;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Column(nullable = false)
    private Boolean paid;

    @Column(name = "participant_limit", nullable = false)
    private Integer participantLimit; // (0 - нет лимита)

    @Column(name = "request_moderation", nullable = false)
    private Boolean requestModeration;  // нужна ли пре-модерация заявок

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventState state; // PENDING,PUBLISHED, CANCELED

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "initiator_id", nullable = false)
//    private User initiator;

    // ID для  разделения вместо User initiator ////////////////////////////////////////////////////////
    @Column(name = "initiator_id")
    private Long initiatorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Embedded
    private Location location;
}