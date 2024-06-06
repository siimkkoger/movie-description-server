package com.example.moviedescriptionsserver;

import com.example.moviedescriptionsserver.repository.CategoryRepository;
import com.example.moviedescriptionsserver.repository.MovieCategoryBridgeRepository;
import com.example.moviedescriptionsserver.repository.MovieRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(applicationName).isEqualTo("shopping-service-test");
    }

}
