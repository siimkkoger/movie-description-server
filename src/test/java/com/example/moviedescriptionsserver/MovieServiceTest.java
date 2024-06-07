package com.example.moviedescriptionsserver;

import com.example.moviedescriptionsserver.dto.request.CreateMovieRequest;
import com.example.moviedescriptionsserver.dto.request.UpdateMovieRequest;
import com.example.moviedescriptionsserver.dto.response.GetMovieResponse;
import com.example.moviedescriptionsserver.entity.*;
import com.example.moviedescriptionsserver.repository.CategoryRepository;
import com.example.moviedescriptionsserver.repository.MovieCategoryBridgeRepository;
import com.example.moviedescriptionsserver.repository.MovieRepository;
import com.example.moviedescriptionsserver.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MovieCategoryBridgeRepository movieCategoryBridgeRepository;

    @InjectMocks
    private MovieService movieService;

    @Captor
    private ArgumentCaptor<MovieEntity> movieEntityCaptor;

    @Captor
    private ArgumentCaptor<List<MovieCategoryEntity>> movieCategoryEntityCaptor;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetMovie() {
        // Given
        String eidrCode = "1234";
        MovieEntity movieEntity = new MovieEntity();
        movieEntity.setEidrCode(eidrCode);
        movieEntity.setName("Movie 1");
        movieEntity.setRating(4.5);
        movieEntity.setYear(2021);
        movieEntity.setStatus(MovieStatus.ACTIVE);

        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setId(1L);
        categoryEntity.setName("Category 1");
        CategoryEntity categoryEntity2 = new CategoryEntity();
        categoryEntity2.setId(2L);
        categoryEntity2.setName("Category 2");
        List<CategoryEntity> categoryEntities = List.of(categoryEntity, categoryEntity2);

        given(movieRepository.findByEidrCode(eidrCode)).willReturn(movieEntity);
        given(categoryRepository.findCategoriesByMovieEidrCode(eidrCode)).willReturn(categoryEntities);

        // When
        GetMovieResponse response = movieService.getMovie(eidrCode);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.movie().eidrCode()).isEqualTo(eidrCode);
        assertThat(response.movie().name()).isEqualTo("Movie 1");
        assertThat(response.movie().rating()).isEqualTo(4.5);
        assertThat(response.movie().year()).isEqualTo(2021);
        assertThat(response.movie().status()).isEqualTo(MovieStatus.ACTIVE);
        assertThat(response.categories()).hasSize(2);
        assertThat(response.categories().get(0).name()).isEqualTo("Category 1");
        assertThat(response.categories().get(1).name()).isEqualTo("Category 2");
    }

    @Test
    void testCreateMovie() {
        // Given
        CreateMovieRequest createMovieRequest = new CreateMovieRequest(
                "1234",
                "Movie 1",
                4.5,
                2021,
                MovieStatus.ACTIVE,
                List.of(1L, 2L)
        );

        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setId(1L);
        categoryEntity.setName("Category 1");
        CategoryEntity categoryEntity2 = new CategoryEntity();
        categoryEntity2.setId(2L);
        categoryEntity2.setName("Category 2");
        List<CategoryEntity> categoryEntities = List.of(categoryEntity, categoryEntity2);

        MovieEntity savedMovieEntity = new MovieEntity();
        savedMovieEntity.setEidrCode(createMovieRequest.eidrCode());
        savedMovieEntity.setName(createMovieRequest.name());
        savedMovieEntity.setRating(createMovieRequest.rating());
        savedMovieEntity.setYear(createMovieRequest.year());
        savedMovieEntity.setStatus(createMovieRequest.status());

        given(movieRepository.findByEidrCode(createMovieRequest.eidrCode())).willReturn(null);
        given(categoryRepository.findCategoriesByIds(createMovieRequest.categories())).willReturn(categoryEntities);
        given(movieRepository.save(any(MovieEntity.class))).willReturn(savedMovieEntity);

        // When
        GetMovieResponse response = movieService.createMovie(createMovieRequest);

        // Then
        verify(movieRepository).save(movieEntityCaptor.capture());
        verify(movieCategoryBridgeRepository).saveAll(movieCategoryEntityCaptor.capture());

        MovieEntity capturedMovie = movieEntityCaptor.getValue();
        List<MovieCategoryEntity> capturedMovieCategories = movieCategoryEntityCaptor.getValue();

        assertThat(capturedMovie.getEidrCode()).isEqualTo("1234");
        assertThat(capturedMovie.getName()).isEqualTo("Movie 1");
        assertThat(capturedMovie.getRating()).isEqualTo(4.5);
        assertThat(capturedMovie.getYear()).isEqualTo(2021);
        assertThat(capturedMovie.getStatus()).isEqualTo(MovieStatus.ACTIVE);

        assertThat(capturedMovieCategories).hasSize(2);
        assertThat(capturedMovieCategories.get(0).getId().getCategoryId()).isEqualTo(1L);
        assertThat(capturedMovieCategories.get(1).getId().getCategoryId()).isEqualTo(2L);

        assertThat(response.movie().eidrCode()).isEqualTo("1234");
        assertThat(response.categories()).hasSize(2);
        assertThat(response.categories().get(0).name()).isEqualTo("Category 1");
        assertThat(response.categories().get(1).name()).isEqualTo("Category 2");
    }

    @Test
    void testUpdateMovie() {
        // Given
        UpdateMovieRequest updateMovieRequest = new UpdateMovieRequest(
                "1234",
                "Updated Movie",
                4.5,
                2021,
                MovieStatus.INACTIVE,
                List.of(1L, 2L)
        );

        MovieEntity existingMovieEntity = new MovieEntity();
        existingMovieEntity.setEidrCode("1234");
        existingMovieEntity.setName("Movie 1");
        existingMovieEntity.setRating(4.5);
        existingMovieEntity.setYear(2021);
        existingMovieEntity.setStatus(MovieStatus.ACTIVE);

        MovieEntity updatedMovieEntity = new MovieEntity();
        updatedMovieEntity.setEidrCode("1234");
        updatedMovieEntity.setName("Updated Movie");
        updatedMovieEntity.setRating(4.5);
        updatedMovieEntity.setYear(2021);
        updatedMovieEntity.setStatus(MovieStatus.INACTIVE);

        CategoryEntity categoryEntity1 = new CategoryEntity();
        categoryEntity1.setId(1L);
        categoryEntity1.setName("Category 1");

        CategoryEntity categoryEntity2 = new CategoryEntity();
        categoryEntity2.setId(2L);
        categoryEntity2.setName("Category 2");

        List<CategoryEntity> categoryEntities = List.of(categoryEntity1, categoryEntity2);

        given(movieRepository.findByEidrCode(updateMovieRequest.eidrCode())).willReturn(existingMovieEntity);
        given(categoryRepository.findCategoriesByIds(updateMovieRequest.categories())).willReturn(categoryEntities);
        given(movieRepository.save(any(MovieEntity.class))).willReturn(updatedMovieEntity);

        // When
        GetMovieResponse response = movieService.updateMovie(updateMovieRequest);

        // Then
        verify(movieRepository).save(movieEntityCaptor.capture());
        verify(movieCategoryBridgeRepository).deleteAll(any());
        verify(movieCategoryBridgeRepository).saveAll(movieCategoryEntityCaptor.capture());

        MovieEntity capturedMovie = movieEntityCaptor.getValue();
        List<MovieCategoryEntity> capturedMovieCategories = movieCategoryEntityCaptor.getValue();

        assertThat(capturedMovie.getEidrCode()).isEqualTo("1234");
        assertThat(capturedMovie.getName()).isEqualTo("Updated Movie");
        assertThat(capturedMovie.getRating()).isEqualTo(4.5);
        assertThat(capturedMovie.getYear()).isEqualTo(2021);
        assertThat(capturedMovie.getStatus()).isEqualTo(MovieStatus.INACTIVE);

        assertThat(capturedMovieCategories).hasSize(2);
        assertThat(capturedMovieCategories.get(0).getId().getCategoryId()).isEqualTo(1L);
        assertThat(capturedMovieCategories.get(1).getId().getCategoryId()).isEqualTo(2L);

        assertThat(response.movie().eidrCode()).isEqualTo("1234");
        assertThat(response.categories()).hasSize(2);
        assertThat(response.categories().get(0).name()).isEqualTo("Category 1");
        assertThat(response.categories().get(1).name()).isEqualTo("Category 2");
    }


    @Test
    void testDeleteMovies() {
        // Given
        List<String> eidrCodes = List.of("1234");

        MovieEntity movieEntity = new MovieEntity();
        movieEntity.setEidrCode("1234");
        movieEntity.setName("Movie 1");

        List<MovieEntity> movieEntities = List.of(movieEntity);

        given(movieRepository.findAllById(eidrCodes)).willReturn(movieEntities);

        // When
        boolean result = movieService.deleteMovies(eidrCodes);

        // Then
        verify(movieCategoryBridgeRepository).deleteAll(any());
        verify(movieRepository).deleteAllById(eidrCodes);

        assertThat(result).isTrue();
    }
}
