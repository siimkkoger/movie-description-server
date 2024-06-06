package com.example.moviedescriptionsserver;

import com.example.moviedescriptionsserver.dto.CreateMovieRequest;
import com.example.moviedescriptionsserver.dto.GetMovieResponse;
import com.example.moviedescriptionsserver.entity.MovieCategoryEntityId;
import com.example.moviedescriptionsserver.repository.CategoryRepository;
import com.example.moviedescriptionsserver.repository.MovieCategoryBridgeRepository;
import com.example.moviedescriptionsserver.repository.MovieRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Test
    void test() {
        assertThat(applicationName).isEqualTo("movie-descriptions-server-test");
    }

    @Test
    void testCreateMovie() throws Exception {
        // Given
        var createMovieRequest = new CreateMovieRequest(
                "eidrCode_test",
                "name",
                5.0,
                2021,
                MovieStatus.ACTIVE,
                List.of(1L)
        );

        // When
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post(controllerPath + "/create-movie")
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createMovieRequest))
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        var response = objectMapper.readValue(result.getResponse().getContentAsString(), GetMovieResponse.class);
        assertThat(response).isNotNull();
        assertThat(response.movie().eidrCode()).isEqualTo(createMovieRequest.eidrCode());
        assertThat(response.movie().name()).isEqualTo(createMovieRequest.name());
        assertThat(response.movie().rating()).isEqualTo(createMovieRequest.rating());
        assertThat(response.movie().year()).isEqualTo(createMovieRequest.year());
        assertThat(response.movie().status()).isEqualTo(createMovieRequest.status());
        assertThat(response.categories()).hasSize(1);
        assertThat(response.categories().get(0).id()).isEqualTo(1L);

        // Cleanup
        var movieCategoryBridgeId = new MovieCategoryEntityId("eidrCode_test", 1L);
        movieCategoryBridgeRepository.deleteById(movieCategoryBridgeId);
        movieRepository.deleteById("eidrCode_test");
        assertThat(movieRepository.findById("eidrCode_test")).isEmpty();
        assertThat(movieCategoryBridgeRepository.findById(movieCategoryBridgeId)).isEmpty();
    }

    @Test
    void testCreateMovie_invalidInput() throws Exception {
        // Given
        var createMovieRequest = new CreateMovieRequest(
                "eidrCode_test",
                "name",
                5.0,
                2021,
                MovieStatus.ACTIVE,
                List.of()
        );

        var createMovieRequestList = new ArrayList<CreateMovieRequest>();
        createMovieRequestList.add(new CreateMovieRequest(null, "name", 5.0, 2021, MovieStatus.ACTIVE, List.of(1L)));
        createMovieRequestList.add(new CreateMovieRequest("eidrCode_test", null, 5.0, 2021, MovieStatus.ACTIVE, List.of(1L)));
        createMovieRequestList.add(new CreateMovieRequest("eidrCode_test", "name", null, 2021, MovieStatus.ACTIVE, List.of(1L)));
        createMovieRequestList.add(new CreateMovieRequest("eidrCode_test", "name", 5.0, null, MovieStatus.ACTIVE, List.of(1L)));
        createMovieRequestList.add(new CreateMovieRequest("eidrCode_test", "name", 5.0, 2021, null, List.of(1L)));
        createMovieRequestList.add(new CreateMovieRequest("eidrCode_test", "name", 5.0, 2021, MovieStatus.ACTIVE, List.of()));


        // When
        createMovieRequestList.forEach(request -> {
            try {
                MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                                .post(controllerPath + "/create-movie")
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType("application/json"))
                        .andExpect(status().isBadRequest())
                        .andReturn();

                // Then
                assertThat(result.getResolvedException()).isNotNull();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
