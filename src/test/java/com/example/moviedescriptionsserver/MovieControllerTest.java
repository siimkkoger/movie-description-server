package com.example.moviedescriptionsserver;

import com.example.moviedescriptionsserver.controller.MovieController;
import com.example.moviedescriptionsserver.dto.response.CategoryResponse;
import com.example.moviedescriptionsserver.dto.request.CreateMovieRequest;
import com.example.moviedescriptionsserver.dto.response.GetMovieResponse;
import com.example.moviedescriptionsserver.dto.MovieDto;
import com.example.moviedescriptionsserver.service.MovieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;

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
    public void testCreateMovie() throws Exception {
        // Given
        final MovieDto movie = new MovieDto("1234", "Movie 1", 4.5, 2021, MovieStatus.ACTIVE);
        final List<CategoryResponse> categories = List.of(new CategoryResponse(1L, "Category 1"));
        final GetMovieResponse expectedResponse = new GetMovieResponse(movie, categories);
        final CreateMovieRequest createMovieRequest = new CreateMovieRequest("1234", "Movie 1", 4.5, 2021, MovieStatus.ACTIVE, List.of(1L));
        given(movieService.createMovie(createMovieRequest)).willReturn(expectedResponse);

        // When
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post(controllerPath + "/create-movie")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createMovieRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        assertThat(result.getResponse().getContentAsString()).isEqualTo(objectMapper.writeValueAsString(expectedResponse));
    }

    @Test
    public void testCreateMovie_invalidInput() throws Exception {
        // Given
        final List<CreateMovieRequest> invalidInputs = List.of(
                new CreateMovieRequest(null, "Movie 1", 4.5, 2021, MovieStatus.ACTIVE, List.of(1L)),
                new CreateMovieRequest("1234", null, 4.5, 2021, MovieStatus.ACTIVE, List.of(1L)),
                new CreateMovieRequest("1234", "Movie 1", null, 2021, MovieStatus.ACTIVE, List.of(1L)),
                new CreateMovieRequest("1234", "Movie 1", 4.5, null, MovieStatus.ACTIVE, List.of(1L)),
                new CreateMovieRequest("1234", "Movie 1", 4.5, 2021, null, List.of(1L)),
                new CreateMovieRequest("1234", "Movie 1", 4.5, 2021, MovieStatus.ACTIVE, null),
                new CreateMovieRequest("1234", "Movie 1", 4.5, 2021, MovieStatus.ACTIVE, List.of())
        );

        invalidInputs.forEach(input -> {
            try {
                // When
                MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                                .post(controllerPath + "/create-movie")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(input)))
                        // TODO : figure out why validation doesn't trigger exception
                        .andExpect(status().isBadRequest())
                        .andReturn();
                // Then
                assertThat(result.getResolvedException()).isInstanceOf(MethodArgumentNotValidException.class);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });


    }

}
