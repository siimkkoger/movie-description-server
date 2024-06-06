package com.example.moviedescriptionsserver;

import com.example.moviedescriptionsserver.controller.MovieController;
import com.example.moviedescriptionsserver.dto.GetMovieResponse;
import com.example.moviedescriptionsserver.service.MovieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MovieController.class)
class MovieControllerTest {

    private final String controllerPath = "/api/movie";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MovieService movieService;

    @Test
    public void testGetMovieByEidrCode() throws Exception {
        // Given
        final String eidrCode = "1234";
        final GetMovieResponse expectedResponse = new GetMovieResponse("1234", "Movie 1", 4.5, 2021, true);
        given(movieService.getMovieByEidrCode("1234")).willReturn(expectedResponse);

        // When
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .get(controllerPath + "/get-by-eidr/%s".formatted(eidrCode))
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        assertThat(result.getResponse().getContentAsString()).isEqualTo(objectMapper.writeValueAsString(expectedResponse));
    }

    @Test
    public void testGetMoviesByName() throws Exception {
        // Given
        final String name = "Movie 1";
        final GetMovieResponse movie1 = new GetMovieResponse("1234", "Movie 1", 4.5, 2021, true);
        final GetMovieResponse movie2 = new GetMovieResponse("5678", "Movie 1", 4.0, 2020, false);

        given(movieService.getMovieByName("Movie 1")).willReturn(List.of(movie1, movie2));

        // When
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .get(controllerPath + "/get-by-name/%s".formatted(name))
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        assertThat(result.getResponse().getContentAsString()).isEqualTo(objectMapper.writeValueAsString(List.of(movie1, movie2)));
    }

}
