package com.example.moviedescriptionsserver.service;

import com.example.moviedescriptionsserver.dto.CategoryResponse;
import com.example.moviedescriptionsserver.dto.CreateMovieRequest;
import com.example.moviedescriptionsserver.dto.GetMovieResponse;
import com.example.moviedescriptionsserver.dto.UpdateMovieRequest;
import com.example.moviedescriptionsserver.entity.CategoryEntity;
import com.example.moviedescriptionsserver.entity.MovieCategoryEntity;
import com.example.moviedescriptionsserver.entity.MovieEntity;
import com.example.moviedescriptionsserver.repository.CategoryRepository;
import com.example.moviedescriptionsserver.repository.MovieCategoryBridgeRepository;
import com.example.moviedescriptionsserver.repository.MovieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MovieService {

    private MovieRepository movieRepository;
    private CategoryRepository categoryRepository;
    private MovieCategoryBridgeRepository movieCategoryBridgeRepository;

    public MovieService(MovieRepository movieRepository, CategoryRepository categoryRepository, MovieCategoryBridgeRepository movieCategoryBridgeRepository) {
        this.movieRepository = movieRepository;
        this.categoryRepository = categoryRepository;
        this.movieCategoryBridgeRepository = movieCategoryBridgeRepository;
    }

    public GetMovieResponse getMovieByEidrCode(String eidrCode) {
        final MovieEntity movieEntity = movieRepository.findByEidrCode(eidrCode);
        final List<CategoryEntity> categoryEntities = categoryRepository.findCategoriesByMovieEidrCode(eidrCode);

        return convertToMovieResponse(movieEntity, categoryEntities);
    }

    public List<GetMovieResponse> getMovieByName(String name) {
        List<MovieEntity> movieEntities = movieRepository.findByName(name);
        return movieEntities.stream()
                .map(movieEntity -> {
                    List<CategoryEntity> categoryEntities = categoryRepository.findCategoriesByMovieEidrCode(movieEntity.getEidrCode());
                    return convertToMovieResponse(movieEntity, categoryEntities);
                })
                .toList();
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.SERIALIZABLE)
    public GetMovieResponse createMovie(CreateMovieRequest createMovieRequest) {
        if (createMovieRequest.categories() == null || createMovieRequest.categories().isEmpty()) {
            throw new IllegalArgumentException("Movie has to have at least one category.");
        }
        List<CategoryEntity> categoryEntities = categoryRepository.findCategoriesByIds(createMovieRequest.categories());
        if (categoryEntities.size() != createMovieRequest.categories().size()) {
            throw new IllegalArgumentException("Some categories do not exist.");
        }

        MovieEntity movie = new MovieEntity();
        movie.setEidrCode(createMovieRequest.eidrCode());
        movie.setName(createMovieRequest.name());
        movie.setRating(createMovieRequest.rating());
        movie.setYear(createMovieRequest.year());
        movie.setStatus(createMovieRequest.status());
        MovieEntity savedMovieEntity = movieRepository.save(movie);

        movieCategoryBridgeRepository.saveAll(
                categoryEntities.stream()
                        .map(categoryEntity -> {
                            var movieCategoryEntity = new MovieCategoryEntity();
                            movieCategoryEntity.getId().setMovieEidr(savedMovieEntity.getEidrCode());
                            movieCategoryEntity.getId().setCategoryId(categoryEntity.getId());
                            return movieCategoryEntity;
                        })
                        .toList()
        );

        return convertToMovieResponse(savedMovieEntity, categoryEntities);
    }

    public GetMovieResponse updateMovie(UpdateMovieRequest updateMovieRequest) {
        return null;
    }

    private GetMovieResponse convertToMovieResponse(MovieEntity movieEntity, List<CategoryEntity> categoryEntities) {
        return new GetMovieResponse(
                movieEntity.getEidrCode(),
                movieEntity.getName(),
                movieEntity.getRating(),
                movieEntity.getYear(),
                movieEntity.getStatus(),
                categoryEntities.stream().map(this::convertToCategoryResponse).toList()
        );
    }

    private CategoryResponse convertToCategoryResponse(CategoryEntity categoryEntity) {
        return new CategoryResponse(
                categoryEntity.getId(),
                categoryEntity.getName()
        );
    }
}
