package ru.practicum.eventsService.event.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.common.dto.events.EventFullDto;
import ru.practicum.common.dto.events.EventShortDto;
import ru.practicum.common.dto.events.Location;
import ru.practicum.eventsService.categories.model.Category;
import ru.practicum.eventsService.categories.repository.CategoryRepository;
import ru.practicum.eventsService.client.UserClient;
import ru.practicum.eventsService.event.dto.NewEventDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.config.import=",
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.cloud.openfeign.circuitbreaker.enabled=true"
})
@ActiveProfiles("test")
class EventServiceFallbackTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    private UserClient userClient;

    private Long userId = 1L;
    private Long categoryId;

    @BeforeEach
    void setUp() {


        jdbcTemplate.execute("DELETE FROM events");
        jdbcTemplate.execute("DELETE FROM categories");


        Category category = Category.builder()
                .name("Test Category")
                .build();

        category = categoryRepository.postCategory(category);
        categoryId = category.getId();
    }

    @Test
    @DisplayName("При недоступности user-service возвращает заглушку, а не падает")
    void shouldReturnFallbackWhenUserServiceIsUnavailable() {

        NewEventDto newEventDto = NewEventDto.builder()
                .annotation("Test annotation")
                .category(categoryId)
                .description("Test description")
                .eventDate(LocalDateTime.now().plusHours(3))
                .location(Location.builder()
                        .lat(55.75f)
                        .lon(37.62f)
                        .build())
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .title("Test Event Fallback")
                .build();


        EventFullDto createdEvent = eventService.createEvent(userId, newEventDto);


        assertThat(createdEvent).isNotNull();

        assertThat(createdEvent.getInitiator()).isNotNull();

        assertThat(createdEvent.getInitiator().getId()).isEqualTo(userId);

        assertThat(createdEvent.getInitiator().getName()).isEqualTo("Unavailable");
    }

    @Test
    @DisplayName("При недоступности user-service при получении списка событий возвращает пользователей-заглушек")
    void shouldReturnFallbackUsersWhenGetUsersDataByIdsFails() {

        NewEventDto newEventDto = NewEventDto.builder()
                .annotation("Test annotation")
                .category(categoryId)
                .description("Test description")
                .eventDate(LocalDateTime.now().plusHours(3))
                .location(Location.builder()
                        .lat(55.75f)
                        .lon(37.62f)
                        .build())
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .title("Test Event Batch Fallback")
                .build();


        eventService.createEvent(userId, newEventDto);


        List<EventShortDto> events =
                eventService.getUserEvents(userId, 0, 10);


        assertThat(events)
                .isNotEmpty();

        EventShortDto event = events.getFirst();


        assertThat(event.getInitiator())
                .isNotNull();

        assertThat(event.getInitiator().getId())
                .isEqualTo(userId);

        assertThat(event.getInitiator().getName())
                .isEqualTo("Unavailable");
    }
}