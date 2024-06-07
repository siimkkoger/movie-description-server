package com.example.moviedescriptionsserver.service;

import com.example.moviedescriptionsserver.MoviesOrderBy;
import com.example.moviedescriptionsserver.dto.MovieDto;
import com.example.moviedescriptionsserver.dto.MovieTableRowDto;
import com.example.moviedescriptionsserver.dto.request.CreateMovieRequest;
import com.example.moviedescriptionsserver.dto.request.GetMoviesFilter;
import com.example.moviedescriptionsserver.dto.request.UpdateMovieRequest;
import com.example.moviedescriptionsserver.dto.response.*;
import com.example.moviedescriptionsserver.entity.*;
import com.example.moviedescriptionsserver.repository.CategoryRepository;
import com.example.moviedescriptionsserver.repository.MovieCategoryBridgeRepository;
import com.example.moviedescriptionsserver.repository.MovieRepository;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieService {

    static Logger logger = LoggerFactory.getLogger(MovieService.class);
    private final JPAQueryFactory queryFactory;
    private final MovieRepository movieRepository;
    private final CategoryRepository categoryRepository;
    private final MovieCategoryBridgeRepository movieCategoryBridgeRepository;

    public MovieService(
            EntityManager entityManager,
            MovieRepository movieRepository,
            CategoryRepository categoryRepository,
            MovieCategoryBridgeRepository movieCategoryBridgeRepository
    ) {
        this.movieRepository = movieRepository;
        this.categoryRepository = categoryRepository;
        this.movieCategoryBridgeRepository = movieCategoryBridgeRepository;
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    public GetMovieResponse getMovie(String eidrCode) {
        MovieEntity movieEntity = movieRepository.findByEidrCode(eidrCode);
        if (movieEntity == null) {
            logger.error("Movie with eidrCode {} does not exist.", eidrCode);
            throw new IllegalArgumentException("Movie with eidrCode " + eidrCode + " does not exist.");
        }

        List<CategoryEntity> categoryEntities = categoryRepository.findCategoriesByMovieEidrCode(eidrCode);
        return convertToMovieResponse(movieEntity, categoryEntities);
    }

    /**
     * Get all movies with the given filters using QueryDSL
     *
     * Note:
     * Uses the string aggregation template for categories using PostgreSQL's string_agg
     * In real life situation I'd possibly use a more generic approach for this as this is PostgreSQL specific.
     * Maybe have a separate query through separate API for categories and then join them with the movies
     * in the frontend. This would be more flexible (separation of concerms), faster (could do async), and easier to maintain.
     * Also, seeing all the categories in the table might not be necessary anyways, but it's good for testing.
     * Anyways... as it wasn't a requirement then I took some liberties with it.
     *
     * @param filter
     * @return
     */
    public GetMovieTableResult getAllMovies(GetMoviesFilter filter) {
        var m = QMovieEntity.movieEntity;
        var c = QCategoryEntity.categoryEntity;
        var mc = QMovieCategoryEntity.movieCategoryEntity;

        BooleanExpression condition = Expressions.asBoolean(true).isTrue();

        // Filters
        if (filter.categoryIds() != null && !filter.categoryIds().isEmpty()) {
            condition = condition.and(mc.id.categoryId.in(filter.categoryIds()));
        }
        if (filter.name() != null) {
            condition = condition.and(m.name.containsIgnoreCase(filter.name()));
        }
        if (filter.eidrCode() != null) {
            condition = condition.and(m.eidrCode.containsIgnoreCase(filter.eidrCode()));
        }

        // Pagination info
        long totalItems = queryFactory
                .selectFrom(m)
                .join(mc).on(m.eidrCode.eq(mc.id.movieEidr))
                .join(c).on(mc.id.categoryId.eq(c.id))
                .where(condition)
                .groupBy(m.eidrCode)
                .fetchCount();
        int totalPages = (int) Math.ceil((double) totalItems / filter.pageSize());

        if (totalItems == 0) {
            return new GetMovieTableResult(new ArrayList<>(), filter.page(), filter.pageSize(), totalItems, totalPages);
        }
        var offset = (filter.page() - 1) * filter.pageSize();
        var orderSpecifier = orderSpecifier(filter.orderBy(), filter.direction());

        // Define the string aggregation template for categories using PostgreSQL's string_agg
        StringTemplate categoryConcat = Expressions.stringTemplate("string_agg({0}, ', ')", c.name);

        var movieList = queryFactory
                .select(Projections.constructor(MovieTableRowDto.class,
                        m.eidrCode,
                        m.name,
                        m.rating,
                        m.year,
                        m.status,
                        categoryConcat.as("categories")
                ))
                .from(m)
                .join(mc).on(m.eidrCode.eq(mc.id.movieEidr))
                .join(c).on(mc.id.categoryId.eq(c.id))
                .where(condition)
                .groupBy(m.eidrCode)
                .orderBy(orderSpecifier)
                .offset(offset)
                .limit(filter.pageSize())
                .fetch();

        return new GetMovieTableResult(movieList, filter.page(), filter.pageSize(), totalItems, totalPages);
    }

    private OrderSpecifier<?> orderSpecifier(MoviesOrderBy orderBy, Order direction) {
        var m = QMovieEntity.movieEntity;
        return switch (orderBy) {
            case NAME ->
                    direction.equals(Order.ASC) ? new OrderSpecifier<>(Order.ASC, m.name) : new OrderSpecifier<>(Order.DESC, m.name);
            case RATING ->
                    direction.equals(Order.ASC) ? new OrderSpecifier<>(Order.ASC, m.rating) : new OrderSpecifier<>(Order.DESC, m.rating);
        };
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.SERIALIZABLE)
    public GetMovieResponse createMovie(CreateMovieRequest createMovieRequest) {
        if (movieRepository.findByEidrCode(createMovieRequest.eidrCode()) != null) {
            logger.error("Movie with eidrCode {} already exists.", createMovieRequest.eidrCode());
            throw new IllegalArgumentException("Movie with eidrCode " + createMovieRequest.eidrCode() + " already exists.");
        }
        validateCreateOrUpdateMovieRequest(createMovieRequest.year(), createMovieRequest.categories());

        // Save movie
        MovieEntity movie = new MovieEntity();
        movie.setEidrCode(createMovieRequest.eidrCode());
        movie.setName(createMovieRequest.name());
        movie.setRating(createMovieRequest.rating());
        movie.setYear(createMovieRequest.year());
        movie.setStatus(createMovieRequest.status());
        MovieEntity savedMovieEntity = movieRepository.save(movie);

        // Save categories
        List<CategoryEntity> categoryEntities = categoryRepository.findCategoriesByIds(createMovieRequest.categories());
        saveMovieCategories(savedMovieEntity, categoryEntities);

        return convertToMovieResponse(savedMovieEntity, categoryEntities);
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.SERIALIZABLE)
    public GetMovieResponse updateMovie(UpdateMovieRequest updateMovieRequest) {
        MovieEntity movieEntity = movieRepository.findByEidrCode(updateMovieRequest.eidrCode());
        if (movieEntity == null) {
            logger.error("Movie with eidrCode {} does not exist.", updateMovieRequest.eidrCode());
            throw new IllegalArgumentException("Movie with eidrCode " + updateMovieRequest.eidrCode() + " does not exist.");
        }
        validateCreateOrUpdateMovieRequest(updateMovieRequest.year(), updateMovieRequest.categories());

        // Update movie (if needed)
        movieEntity.setName(updateMovieRequest.name());
        movieEntity.setRating(updateMovieRequest.rating());
        movieEntity.setYear(updateMovieRequest.year());
        movieEntity.setStatus(updateMovieRequest.status());
        if (!currentMovieFieldsEqualRequestedFields(movieEntity, updateMovieRequest)) {
            movieRepository.save(movieEntity);
        }

        List<CategoryEntity> categoriesRequested = categoryRepository.findCategoriesByIds(updateMovieRequest.categories());
        // Update categories (if needed)
        List<CategoryEntity> categoriesCurrent = categoryRepository.findCategoriesByMovieEidrCode(updateMovieRequest.eidrCode());
        if (!currentMovieCategoriesEqualRequestedCategories(categoriesCurrent, updateMovieRequest.categories())) {
            // Remove everything from the bridge table and add the new ones
            movieCategoryBridgeRepository.deleteAll(movieCategoryBridgeRepository.findAllByMovieEidrCodes(List.of(updateMovieRequest.eidrCode())));
            saveMovieCategories(movieEntity, categoriesRequested);
        }

        return convertToMovieResponse(movieEntity, categoriesRequested);
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.SERIALIZABLE)
    public boolean deleteMovies(List<String> eidrCodes) {
        // Check if all movies exist
        List<MovieEntity> movieEntities = movieRepository.findAllById(eidrCodes);
        if (movieEntities.size() != eidrCodes.size()) {
            logger.error("Some of the movies with the given eidrCodes do not exist.");
            throw new IllegalArgumentException("Some of the movies with the given eidrCodes do not exist.");
        }

        // Delete from the bridge table
        movieCategoryBridgeRepository.deleteAll(movieCategoryBridgeRepository.findAllByMovieEidrCodes(eidrCodes));

        // Delete from the movie table
        movieRepository.deleteAllById(eidrCodes);

        return true;
    }

    /**
     * Checks if the current movie fields are equal to the requested fields
     * So that there wouldn't be unnecessary updates
     *
     * @param movieEntity
     * @param updateMovieRequest
     * @return
     */
    private boolean currentMovieFieldsEqualRequestedFields(MovieEntity movieEntity, UpdateMovieRequest updateMovieRequest) {
        return movieEntity.getEidrCode().equals(updateMovieRequest.eidrCode()) &&
                movieEntity.getName().equals(updateMovieRequest.name()) &&
                movieEntity.getRating().equals(updateMovieRequest.rating()) &&
                movieEntity.getYear().equals(updateMovieRequest.year()) &&
                movieEntity.getStatus().equals(updateMovieRequest.status());
    }

    /**
     * Checks if the current movie categories are equal to the requested categories
     * So that there wouldn't be unnecessary updates
     * @param currentCategories
     * @param requestedCategories
     * @return
     */
    private boolean currentMovieCategoriesEqualRequestedCategories(List<CategoryEntity> currentCategories, List<Long> requestedCategories) {
        return currentCategories.stream().map(CategoryEntity::getId).toList().equals(requestedCategories);
    }

    private GetMovieResponse convertToMovieResponse(MovieEntity movieEntity, List<CategoryEntity> categoryEntities) {
        return new GetMovieResponse(
                new MovieDto(
                        movieEntity.getEidrCode(),
                        movieEntity.getName(),
                        movieEntity.getRating(),
                        movieEntity.getYear(),
                        movieEntity.getStatus()
                ),
                categoryEntities.stream().map(this::convertToCategoryResponse).toList()
        );
    }

    private CategoryResponse convertToCategoryResponse(CategoryEntity categoryEntity) {
        return new CategoryResponse(
                categoryEntity.getId(),
                categoryEntity.getName()
        );
    }

    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryEntity -> new CategoryResponse(categoryEntity.getId(), categoryEntity.getName()))
                .toList();
    }

    private void validateCreateOrUpdateMovieRequest(Integer year, List<Long> categories) {
        if (year > Year.now().getValue()) {
            logger.error("Year cannot be in the future.");
            throw new IllegalArgumentException("Year cannot be in the future.");
        }
        if (categories == null || categories.isEmpty()) {
            logger.error("Movie has to have at least one category.");
            throw new IllegalArgumentException("Movie has to have at least one category.");
        }
        List<CategoryEntity> categoryEntities = categoryRepository.findCategoriesByIds(categories);
        if (categoryEntities.size() != categories.size()) {
            logger.error("Some categories do not exist.");
            throw new IllegalArgumentException("Some categories do not exist.");
        }
    }

    private void saveMovieCategories(MovieEntity movie, List<CategoryEntity> categoryEntities) {
        movieCategoryBridgeRepository.saveAll(
                categoryEntities.stream()
                        .map(categoryEntity -> {
                            var MovieCategoryEntityId = new MovieCategoryEntityId();
                            MovieCategoryEntityId.setMovieEidr(movie.getEidrCode());
                            MovieCategoryEntityId.setCategoryId(categoryEntity.getId());

                            var movieCategoryEntity = new MovieCategoryEntity();
                            movieCategoryEntity.setId(MovieCategoryEntityId);
                            return movieCategoryEntity;
                        })
                        .collect(Collectors.toList())
        );
    }
}
