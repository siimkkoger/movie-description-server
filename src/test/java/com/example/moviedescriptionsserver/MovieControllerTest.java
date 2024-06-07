package com.example.moviedescriptionsserver;

import com.example.moviedescriptionsserver.controller.MovieController;
import com.example.moviedescriptionsserver.dto.MovieDto;
import com.example.moviedescriptionsserver.dto.request.CreateMovieRequest;
import com.example.moviedescriptionsserver.dto.request.DeleteMoviesRequest;
import com.example.moviedescriptionsserver.dto.request.GetMoviesFilter;
import com.example.moviedescriptionsserver.dto.request.UpdateMovieRequest;
import com.example.moviedescriptionsserver.dto.response.CategoryResponse;
import com.example.moviedescriptionsserver.dto.response.GetMovieResponse;
import com.example.moviedescriptionsserver.dto.response.GetMovieTableResult;
import com.example.moviedescriptionsserver.dto.MovieTableRowDto;
import com.example.moviedescriptionsserver.service.MovieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.stream.Stream;

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
    void testCreateMovie() throws Exception {
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

    @ParameterizedTest
    @MethodSource("provideInvalidCreateMovieRequests")
    void testCreateMovie_invalidInput(CreateMovieRequest invalidInput) throws Exception {
        // When
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post(controllerPath + "/create-movie")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidInput)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Then
        assertThat(result.getResolvedException()).isInstanceOf(MethodArgumentNotValidException.class);
    }

    private static Stream<Arguments> provideInvalidCreateMovieRequests() {
        return Stream.of(
                Arguments.of(new CreateMovieRequest(null, "Movie 1", 4.5, 2021, MovieStatus.ACTIVE, List.of(1L))),
                Arguments.of(new CreateMovieRequest("1234", null, 4.5, 2021, MovieStatus.ACTIVE, List.of(1L))),
                Arguments.of(new CreateMovieRequest("1234", "Movie 1", null, 2021, MovieStatus.ACTIVE, List.of(1L))),
                Arguments.of(new CreateMovieRequest("1234", "Movie 1", 4.5, null, MovieStatus.ACTIVE, List.of(1L))),
                Arguments.of(new CreateMovieRequest("1234", "Movie 1", 4.5, 2021, null, List.of(1L))),
                Arguments.of(new CreateMovieRequest("1234", "Movie 1", 4.5, 2021, MovieStatus.ACTIVE, null)),
                Arguments.of(new CreateMovieRequest("1234", "Movie 1", 4.5, 2021, MovieStatus.ACTIVE, List.of()))
        );
    }

    @Test
    void testGetMovie() throws Exception {
        // Given
        final MovieDto movie = new MovieDto("1234", "Movie 1", 4.5, 2021, MovieStatus.ACTIVE);
        final List<CategoryResponse> categories = List.of(new CategoryResponse(1L, "Category 1"));
        final GetMovieResponse expectedResponse = new GetMovieResponse(movie, categories);
        given(movieService.getMovie("1234")).willReturn(expectedResponse);

        // When
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .get(controllerPath + "/get-movie")
                        .param("eidrCode", "1234")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        assertThat(result.getResponse().getContentAsString()).isEqualTo(objectMapper.writeValueAsString(expectedResponse));
    }

    @Test
    void testGetMovie_invalidEidrCode() throws Exception {
        // Given
        given(movieService.getMovie("invalid_eidr_code")).willThrow(new IllegalArgumentException("Movie with eidrCode invalid_eidr_code does not exist."));

        // When
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .get(controllerPath + "/get-movie")
                        .param("eidrCode", "invalid_eidr_code")
                        .accept("application/json"))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Then
        assertThat(result.getResolvedException()).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testGetCategories() throws Exception {
        // Given
        final List<CategoryResponse> categories = List.of(
                new CategoryResponse(1L, "Category 1"),
                new CategoryResponse(2L, "Category 2")
        );
        given(movieService.getCategories()).willReturn(categories);

        // When
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .get(controllerPath + "/get-categories")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        assertThat(result.getResponse().getContentAsString()).isEqualTo(objectMapper.writeValueAsString(categories));
    }

    @Test
    void testUpdateMovie() throws Exception {
        // Given
        final MovieDto movie = new MovieDto("1234", "Updated Movie", 4.5, 2021, MovieStatus.INACTIVE);
        final List<CategoryResponse> categories = List.of(new CategoryResponse(1L, "Category 1"));
        final GetMovieResponse expectedResponse = new GetMovieResponse(movie, categories);
        final UpdateMovieRequest updateMovieRequest = new UpdateMovieRequest("1234", "Updated Movie", 4.5, 2021, MovieStatus.INACTIVE, List.of(1L));
        given(movieService.updateMovie(updateMovieRequest)).willReturn(expectedResponse);

        // When
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .put(controllerPath + "/update-movie")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateMovieRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        assertThat(result.getResponse().getContentAsString()).isEqualTo(objectMapper.writeValueAsString(expectedResponse));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidUpdateMovieRequests")
    void testUpdateMovie_invalidInput(UpdateMovieRequest invalidInput) throws Exception {
        // When
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .put(controllerPath + "/update-movie")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidInput)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Then
        assertThat(result.getResolvedException()).isInstanceOf(MethodArgumentNotValidException.class);
    }

    private static Stream<Arguments> provideInvalidUpdateMovieRequests() {
        return Stream.of(
                Arguments.of(new UpdateMovieRequest(null, "Updated Movie", 4.5, 2021, MovieStatus.INACTIVE, List.of(1L))),
                Arguments.of(new UpdateMovieRequest("1234", null, 4.5, 2021, MovieStatus.INACTIVE, List.of(1L))),
                Arguments.of(new UpdateMovieRequest("1234", "Updated Movie", null, 2021, MovieStatus.INACTIVE, List.of(1L))),
                Arguments.of(new UpdateMovieRequest("1234", "Updated Movie", 4.5, null, MovieStatus.INACTIVE, List.of(1L))),
                Arguments.of(new UpdateMovieRequest("1234", "Updated Movie", 4.5, 2021, null, List.of(1L))),
                Arguments.of(new UpdateMovieRequest("1234", "Updated Movie", 4.5, 2021, MovieStatus.INACTIVE, null)),
                Arguments.of(new UpdateMovieRequest("1234", "Updated Movie", 4.5, 2021, MovieStatus.INACTIVE, List.of()))
        );
    }

    @Test
    void testDeleteMovies() throws Exception {
        // Given
        final DeleteMoviesRequest deleteMoviesRequest = new DeleteMoviesRequest(List.of("1234"));
        given(movieService.deleteMovies(deleteMoviesRequest.eidrCodes())).willReturn(true);

        // When
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .delete(controllerPath + "/delete-movies")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(deleteMoviesRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        assertThat(result.getResponse().getContentAsString()).isEqualTo("true");
    }

    @Test
    void testGetMoviesTable() throws Exception {
        // Given
        final List<MovieTableRowDto> movies = List.of(
                new MovieTableRowDto("1234", "Movie 1", 4.5, 2021, MovieStatus.ACTIVE, "Category 1"),
                new MovieTableRowDto("5678", "Movie 2", 3.0, 2020, MovieStatus.INACTIVE, "Category 2")
        );
        final GetMovieTableResult expectedResponse = new GetMovieTableResult(movies, 1, 10, 2L, 1);
        final GetMoviesFilter getMoviesFilter = new GetMoviesFilter(List.of(1L), null, null, 1, 10, MoviesOrderBy.RATING, Order.ASC);
        given(movieService.getAllMovies(getMoviesFilter)).willReturn(expectedResponse);

        // When
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post(controllerPath + "/get-movies-table")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(getMoviesFilter)))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = result.getResponse().getContentAsString();
        GetMovieTableResult actualResponse = objectMapper.readValue(jsonResponse, GetMovieTableResult.class);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.movies()).hasSize(2);

        // Check each movie in the response
        for (int i = 0; i < movies.size(); i++) {
            MovieTableRowDto expectedMovie = movies.get(i);
            MovieTableRowDto actualMovie = actualResponse.movies().get(i);

            assertThat(actualMovie.eidrCode()).isEqualTo(expectedMovie.eidrCode());
            assertThat(actualMovie.name()).isEqualTo(expectedMovie.name());
            assertThat(actualMovie.rating()).isEqualTo(expectedMovie.rating());
            assertThat(actualMovie.year()).isEqualTo(expectedMovie.year());
            assertThat(actualMovie.status()).isEqualTo(expectedMovie.status());
            assertThat(actualMovie.categories()).isEqualTo(expectedMovie.categories());
        }

        // Check pagination info
        assertThat(actualResponse.page()).isEqualTo(expectedResponse.page());
        assertThat(actualResponse.pageSize()).isEqualTo(expectedResponse.pageSize());
        assertThat(actualResponse.totalItems()).isEqualTo(expectedResponse.totalItems());
        assertThat(actualResponse.totalPages()).isEqualTo(expectedResponse.totalPages());
    }


}
