package com.example.moviedescriptionsserver;

import com.example.moviedescriptionsserver.dto.request.CreateMovieRequest;
import com.example.moviedescriptionsserver.dto.request.DeleteMoviesRequest;
import com.example.moviedescriptionsserver.dto.request.GetMoviesFilter;
import com.example.moviedescriptionsserver.dto.request.UpdateMovieRequest;
import com.example.moviedescriptionsserver.dto.response.GetMovieResponse;
import com.example.moviedescriptionsserver.dto.response.GetMovieTableResult;
import com.example.moviedescriptionsserver.entity.MovieCategoryEntityId;
import com.example.moviedescriptionsserver.repository.CategoryRepository;
import com.example.moviedescriptionsserver.repository.MovieCategoryBridgeRepository;
import com.example.moviedescriptionsserver.repository.MovieRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "/application-test.properties")
@AutoConfigureMockMvc
@Sql(scripts = {"/migration/setup-test-schema.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"/migration/teardown-test-schema.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class MovieE2eTest {

    @Value("${spring.application.name}")
    private String applicationName;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MovieCategoryBridgeRepository movieCategoryBridgeRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private final String controllerPath = "/api/movie";

    @BeforeEach
    void setup() {
        assertThat(applicationName).isEqualTo("movie-descriptions-server-test");
    }

    @Test
    void testCreateMovie() throws Exception {
        var createMovieRequest = new CreateMovieRequest(
                "eidrCode_test",
                "name",
                5.0,
                2021,
                MovieStatus.ACTIVE,
                List.of(1L)
        );

        MvcResult result = performPostRequest(controllerPath + "/create-movie", createMovieRequest)
                .andExpect(status().isOk())
                .andReturn();

        var response = objectMapper.readValue(result.getResponse().getContentAsString(), GetMovieResponse.class);
        assertThat(response).isNotNull();
        assertThat(response.movie().eidrCode()).isEqualTo(createMovieRequest.eidrCode());
        assertThat(response.movie().name()).isEqualTo(createMovieRequest.name());
        assertThat(response.movie().rating()).isEqualTo(createMovieRequest.rating());
        assertThat(response.movie().year()).isEqualTo(createMovieRequest.year());
        assertThat(response.movie().status()).isEqualTo(createMovieRequest.status());
        assertThat(response.categories()).hasSize(1);
        assertThat(response.categories().get(0).id()).isEqualTo(1L);

        cleanupMovie(createMovieRequest.eidrCode(), 1L);
    }

    @Test
    void testCreateMovie_invalidInput() throws Exception {
        var createMovieRequestList = new ArrayList<CreateMovieRequest>();
        createMovieRequestList.add(new CreateMovieRequest(null, "name", 5.0, 2021, MovieStatus.ACTIVE, List.of(1L)));
        createMovieRequestList.add(new CreateMovieRequest("eidrCode_test", null, 5.0, 2021, MovieStatus.ACTIVE, List.of(1L)));
        createMovieRequestList.add(new CreateMovieRequest("eidrCode_test", "name", null, 2021, MovieStatus.ACTIVE, List.of(1L)));
        createMovieRequestList.add(new CreateMovieRequest("eidrCode_test", "name", 5.0, null, MovieStatus.ACTIVE, List.of(1L)));
        createMovieRequestList.add(new CreateMovieRequest("eidrCode_test", "name", 5.0, 9999, MovieStatus.ACTIVE, List.of(1L)));
        createMovieRequestList.add(new CreateMovieRequest("eidrCode_test", "name", 5.0, 2021, null, List.of(1L)));
        createMovieRequestList.add(new CreateMovieRequest("eidrCode_test", "name", 5.0, 2021, MovieStatus.ACTIVE, List.of()));
        createMovieRequestList.add(new CreateMovieRequest("eidrCode_test", "name", 5.0, 2021, MovieStatus.ACTIVE, null));

        createMovieRequestList.forEach(request -> {
            try {
                MvcResult result = performPostRequest(controllerPath + "/create-movie", request)
                        .andExpect(status().isBadRequest())
                        .andReturn();

                assertThat(result.getResolvedException()).isNotNull();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    void testUpdateMovie() throws Exception {
        // Given
        var createMovieRequest = new CreateMovieRequest(
                "eidrCode_test",
                "name",
                5.0,
                2021,
                MovieStatus.ACTIVE,
                List.of(1L)
        );

        MvcResult createdResult = performPostRequest(controllerPath + "/create-movie", createMovieRequest)
                .andExpect(status().isOk())
                .andReturn();
        var createdMovieResponse = objectMapper.readValue(createdResult.getResponse().getContentAsString(), GetMovieResponse.class);
        var createdMovieEidrCode = createdMovieResponse.movie().eidrCode();

        var updateMovieRequest = new UpdateMovieRequest(
                createdMovieEidrCode,
                "new name",
                4.0,
                2022,
                MovieStatus.INACTIVE,
                List.of(2L, 4L)
        );

        // When
        performPutRequest(controllerPath + "/update-movie", updateMovieRequest)
                .andExpect(status().isOk())
                .andReturn();

        // Then
        MvcResult getResultByEidr = performPostRequest(controllerPath + "/get-movies-table", new GetMoviesFilter(null, createdMovieEidrCode, null, null, null, null, null))
                .andExpect(status().isOk())
                .andReturn();
        var response = objectMapper.readValue(getResultByEidr.getResponse().getContentAsString(), GetMovieTableResult.class);

        assertThat(response).isNotNull();
        var movie = response.movies().get(0);
        assertThat(movie.eidrCode()).isEqualTo(updateMovieRequest.eidrCode());
        assertThat(movie.name()).isEqualTo(updateMovieRequest.name());
        assertThat(movie.rating()).isEqualTo(updateMovieRequest.rating());
        assertThat(movie.year()).isEqualTo(updateMovieRequest.year());
        assertThat(movie.status()).isEqualTo(updateMovieRequest.status());

        MvcResult getResultByCategory = performPostRequest(controllerPath + "/get-movies-table", new GetMoviesFilter(List.of(2L, 4L), null, null, null, null, null, null))
                .andExpect(status().isOk())
                .andReturn();
        response = objectMapper.readValue(getResultByCategory.getResponse().getContentAsString(), GetMovieTableResult.class);

        assertThat(response).isNotNull();
        assertThat(response.movies().stream().anyMatch(m -> m.eidrCode().equals(updateMovieRequest.eidrCode()))).isTrue();

        // Cleanup
        cleanupMovie(updateMovieRequest.eidrCode(), 2L, 4L);
    }

    @Test
    void testUpdateMovie_invalidInput() throws Exception {
        // Given
        var createMovieRequest = new CreateMovieRequest(
                "eidrCode_test",
                "name",
                5.0,
                2021,
                MovieStatus.ACTIVE,
                List.of(1L)
        );
        MvcResult createdResult = performPostRequest(controllerPath + "/create-movie", createMovieRequest)
                .andExpect(status().isOk())
                .andReturn();
        var createdMovieResponse = objectMapper.readValue(createdResult.getResponse().getContentAsString(), GetMovieResponse.class);
        var createdMovieEidrCode = createdMovieResponse.movie().eidrCode();

        var updateMovieRequestList = new ArrayList<UpdateMovieRequest>();
        updateMovieRequestList.add(new UpdateMovieRequest(null, "new name", 4.0, 2022, MovieStatus.INACTIVE, List.of(2L, 4L)));
        updateMovieRequestList.add(new UpdateMovieRequest(createdMovieEidrCode, null, 4.0, 2022, MovieStatus.INACTIVE, List.of(2L, 4L)));
        updateMovieRequestList.add(new UpdateMovieRequest(createdMovieEidrCode, "new name", null, 2022, MovieStatus.INACTIVE, List.of(2L, 4L)));
        updateMovieRequestList.add(new UpdateMovieRequest(createdMovieEidrCode, "new name", 4.0, null, MovieStatus.INACTIVE, List.of(2L, 4L)));
        updateMovieRequestList.add(new UpdateMovieRequest(createdMovieEidrCode, "new name", 4.0, 9999, MovieStatus.INACTIVE, List.of(2L, 4L)));
        updateMovieRequestList.add(new UpdateMovieRequest(createdMovieEidrCode, "new name", 4.0, 2022, null, List.of(2L, 4L)));
        updateMovieRequestList.add(new UpdateMovieRequest(createdMovieEidrCode, "new name", 4.0, 2022, MovieStatus.INACTIVE, List.of()));
        updateMovieRequestList.add(new UpdateMovieRequest(createdMovieEidrCode, "new name", 4.0, 2022, MovieStatus.INACTIVE, null));

        updateMovieRequestList.forEach(request -> {
            try {
                MvcResult result = performPutRequest(controllerPath + "/update-movie", request)
                        .andExpect(status().isBadRequest())
                        .andReturn();

                assertThat(result.getResolvedException()).isNotNull();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    void testDeleteMovie() throws Exception {
        var createMovieRequest = new CreateMovieRequest(
                "eidrCode_test",
                "name",
                5.0,
                2021,
                MovieStatus.ACTIVE,
                List.of(2L, 4L)
        );

        // Create movie
        MvcResult createdResult = performPostRequest(controllerPath + "/create-movie", createMovieRequest)
                .andExpect(status().isOk())
                .andReturn();
        var createdMovieResponse = objectMapper.readValue(createdResult.getResponse().getContentAsString(), GetMovieResponse.class);
        var createdMovieEidrCode = createdMovieResponse.movie().eidrCode();

        // Delete movie
        performDeleteRequest(controllerPath + "/delete-movies", new DeleteMoviesRequest(List.of(createdMovieEidrCode)))
                .andExpect(status().isOk())
                .andReturn();

        // Check if movie was deleted
        var response = getMovieTable(new GetMoviesFilter(null, createdMovieEidrCode, null, null, null, null, null));

        assertThat(response).isNotNull();
        assertThat(response.movies()).isEmpty();

        // Check if movie categories were deleted
        response = getMovieTable(new GetMoviesFilter(List.of(2L, 4L), null, null, null, null, null, null));
        assertThat(response).isNotNull();
        assertThat(response.movies().stream().anyMatch(m -> m.eidrCode().equals(createdMovieEidrCode))).isFalse();
    }

    @Test
    void testDeleteMovie_invalidInput() throws Exception {
        var deleteMoviesRequest = new DeleteMoviesRequest(List.of("999999999999"));

        MvcResult result = performDeleteRequest(controllerPath + "/delete-movies", deleteMoviesRequest)
                .andExpect(status().isBadRequest()).andReturn();

        assertThat(result.getResolvedException()).isNotNull();
    }

    @Test
    void testGetMovie() throws Exception {
        // Given
        var createMovieRequest = new CreateMovieRequest(
                "eidrCode_test_single",
                "Movie single",
                5.0,
                2021,
                MovieStatus.ACTIVE,
                List.of(2L, 4L)
        );

        performPostRequest(controllerPath + "/create-movie", createMovieRequest);

        // When
        MvcResult getResult = mockMvc.perform(MockMvcRequestBuilders
                        .get(controllerPath + "/get-movie")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("eidrCode", "eidrCode_test_single"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        var response = objectMapper.readValue(getResult.getResponse().getContentAsString(), GetMovieResponse.class);
        assertThat(response).isNotNull();
        assertThat(response.movie().eidrCode()).isEqualTo("eidrCode_test_single");
        assertThat(response.movie().name()).isEqualTo("Movie single");
        assertThat(response.movie().rating()).isEqualTo(5.0);
        assertThat(response.movie().year()).isEqualTo(2021);
        assertThat(response.movie().status()).isEqualTo(MovieStatus.ACTIVE);
        assertThat(response.categories()).hasSize(2);
        assertThat(response.categories().get(0).id()).isEqualTo(2L);
        assertThat(response.categories().get(1).id()).isEqualTo(4L);
    }

    @Test
    void testGetMovies_noInputs() throws Exception {
        var response = getMovieTable(new GetMoviesFilter(null, null, null, null, null, null, null));

        assertThat(response).isNotNull();
        assertThat(response.movies()).hasSize(5);
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.totalPages()).isEqualTo(5);
        assertThat(response.totalItems()).isEqualTo(25);
    }

    @Test
    void testGetMovies_categoryFilter() throws Exception {
        var response = getMovieTable(new GetMoviesFilter(List.of(1L), null, null, null, null, null, null));

        assertThat(response).isNotNull();
        assertThat(response.movies()).hasSize(5);
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.totalPages()).isEqualTo(2);
        assertThat(response.totalItems()).isEqualTo(10);
    }

    @Test
    void testGetMovies_pagination() throws Exception {
        var response = getMovieTable(new GetMoviesFilter(null, null, null, 2, 10, null, null));

        assertThat(response).isNotNull();
        assertThat(response.movies()).hasSize(10);
        assertThat(response.page()).isEqualTo(2);
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.totalItems()).isEqualTo(25);
    }

    @Test
    void testGetMovies_orderBy_desc() throws Exception {
        var response = getMovieTable(new GetMoviesFilter(null, null, null, 1, 7, MoviesOrderBy.RATING, Order.DESC));

        assertThat(response).isNotNull();
        assertThat(response.movies()).hasSize(7);
        assertThat(response.movies().get(0).rating()).isEqualTo(9.0);
        assertThat(response.movies().get(1).rating()).isEqualTo(9.0);
        assertThat(response.movies().get(2).rating()).isEqualTo(9.0);
        assertThat(response.movies().get(3).rating()).isEqualTo(9.0);
        assertThat(response.movies().get(4).rating()).isEqualTo(9.0);
        assertThat(response.movies().get(5).rating()).isEqualTo(9.0);
        assertThat(response.movies().get(6).rating()).isEqualTo(8.0);
    }

    @Test
    void testGetMovies_orderBy_asc() throws Exception {
        var response = getMovieTable(new GetMoviesFilter(null, null, null, 1, 7, MoviesOrderBy.RATING, Order.ASC));

        assertThat(response).isNotNull();
        assertThat(response.movies()).hasSize(7);
        assertThat(response.movies().get(0).rating()).isEqualTo(6.0);
        assertThat(response.movies().get(1).rating()).isEqualTo(6.0);
        assertThat(response.movies().get(2).rating()).isEqualTo(6.0);
        assertThat(response.movies().get(3).rating()).isEqualTo(6.0);
        assertThat(response.movies().get(4).rating()).isEqualTo(6.0);
        assertThat(response.movies().get(5).rating()).isEqualTo(6.0);
        assertThat(response.movies().get(6).rating()).isEqualTo(7.0);
    }

    // Helper methods

    private GetMovieTableResult getMovieTable(GetMoviesFilter filter) throws Exception {
        MvcResult getResult = performPostRequest(controllerPath + "/get-movies-table", filter).andExpect(status().isOk()).andReturn();
        return objectMapper.readValue(getResult.getResponse().getContentAsString(), GetMovieTableResult.class);
    }

    private ResultActions performPostRequest(String url, Object request) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders
                .post(url)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .contentType("application/json"));
    }

    private ResultActions performPutRequest(String url, Object request) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders
                .put(url)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .contentType("application/json"));
    }

    private ResultActions performDeleteRequest(String url, Object request) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders
                .delete(url)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .contentType("application/json"));
    }

    private void cleanupMovie(String eidrCode, Long... categoryIds) {
        for (Long categoryId : categoryIds) {
            movieCategoryBridgeRepository.deleteById(new MovieCategoryEntityId(eidrCode, categoryId));
        }
        movieRepository.deleteById(eidrCode);
        assertThat(movieRepository.findById(eidrCode)).isEmpty();
        for (Long categoryId : categoryIds) {
            assertThat(movieCategoryBridgeRepository.findById(new MovieCategoryEntityId(eidrCode, categoryId))).isEmpty();
        }
    }
}
