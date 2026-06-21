package ru.practicum.stats.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.stat.dto.EndpointHitDto;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StatsControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void shouldCreateHit() throws Exception {
        EndpointHitDto hit = EndpointHitDto.builder()
                .app("ewm-service")
                .uri("/events/1")
                .ip("127.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();

        mvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(hit)))
                .andExpect(status().isCreated());
    }
}