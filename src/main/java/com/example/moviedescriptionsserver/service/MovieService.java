package com.example.moviedescriptionsserver.service;

import com.example.moviedescriptionsserver.MoviesOrderBy;
import com.example.moviedescriptionsserver.dto.*;
import com.example.moviedescriptionsserver.entity.*;
import com.example.moviedescriptionsserver.repository.CategoryRepository;
import com.example.moviedescriptionsserver.repository.MovieCategoryBridgeRepository;
import com.example.moviedescriptionsserver.repository.MovieRepository;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.sql.SQLOps;
import com.querydsl.sql.SQLTemplates;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovieService {

    static Logger logger = LoggerFactory.getLogger(MovieService.class);

    private final JPAQueryFactory queryFactory;

    private final EntityManager entityManager;
    private final MovieRepository movieRepository;
    private final CategoryRepository categoryRepository;
    private final MovieCategoryBridgeRepository movieCategoryBridgeRepository;

    public MovieService(
            EntityManager entityManager,
            MovieRepository movieRepository,
            CategoryRepository categoryRepository,
            MovieCategoryBridgeRepository movieCategoryBridgeRepository
    ) {
        this.entityManager = entityManager;
        this.movieRepository = movieRepository;
        this.categoryRepository = categoryRepository;
        this.movieCategoryBridgeRepository = movieCategoryBridgeRepository;
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    public GetMovieResponse getMovie(String eidrCode) {
        MovieEntity movieEntity = movieRepository.findByEidrCode(eidrCode);
        if (movieEntity == null) {
            throw new IllegalArgumentException("Movie with eidrCode " + eidrCode + " does not exist.");
        }

        List<CategoryEntity> categoryEntities = categoryRepository.findCategoriesByMovieEidrCode(eidrCode);
        return convertToMovieResponse(movieEntity, categoryEntities);
    }

    public GetMovieTableResult getAllMovies(GetMoviesFilter filter) {
        var m = QMovieEntity.movieEntity;
        var c = QCategoryEntity.categoryEntity;
        var mc = QMovieCategoryEntity.movieCategoryEntity;

        BooleanExpression condition = Expressions.asBoolean(true).isTrue();

        // Filter by categories
        if (filter.categoryIds() != null && !filter.categoryIds().isEmpty()) {
            condition = condition.and(mc.id.categoryId.in(filter.categoryIds()));
        }
        // Filter by name
        if (filter.name() != null) {
            condition = condition.and(m.name.containsIgnoreCase(filter.name()));
        }
        // Filter by eidrCode
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
                .select(Projections.constructor(MovieTableRow.class,
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
                .groupBy(m.eidrCode, m.name, m.rating, m.year, m.status)
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
            throw new IllegalArgumentException("Movie with eidrCode " + createMovieRequest.eidrCode() + " already exists.");
        }
        if (createMovieRequest.year() > Year.now().getValue()) {
            throw new IllegalArgumentException("Year cannot be in the future.");
        }
        if (createMovieRequest.categories() == null || createMovieRequest.categories().isEmpty()) {
            throw new IllegalArgumentException("Movie has to have at least one category.");
        }
        List<CategoryEntity> categoryEntities = categoryRepository.findCategoriesByIds(createMovieRequest.categories());
        if (categoryEntities.size() != createMovieRequest.categories().size()) {
            throw new IllegalArgumentException("Some categories do not exist.");
        }

        logger.info("Creating movie with information: {}", createMovieRequest.toString());

        MovieEntity movie = new MovieEntity();
        movie.setEidrCode(createMovieRequest.eidrCode());
        movie.setName(createMovieRequest.name());
        movie.setRating(createMovieRequest.rating());
        movie.setYear(createMovieRequest.year()); // TODO - aasta ei voi olla tulevikus
        movie.setStatus(createMovieRequest.status());
        MovieEntity savedMovieEntity = movieRepository.save(movie);

        movieCategoryBridgeRepository.saveAll(
                categoryEntities.stream()
                        .map(categoryEntity -> {
                            var MovieCategoryEntityId = new MovieCategoryEntityId();
                            MovieCategoryEntityId.setMovieEidr(savedMovieEntity.getEidrCode());
                            MovieCategoryEntityId.setCategoryId(categoryEntity.getId());

                            var movieCategoryEntity = new MovieCategoryEntity();
                            movieCategoryEntity.setId(MovieCategoryEntityId);
                            return movieCategoryEntity;
                        })
                        .toList()
        );

        return convertToMovieResponse(savedMovieEntity, categoryEntities);
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.SERIALIZABLE)
    public GetMovieResponse updateMovie(UpdateMovieRequest updateMovieRequest) {
        MovieEntity movieEntity = movieRepository.findByEidrCode(updateMovieRequest.eidrCode());
        if (movieEntity == null) {
            throw new IllegalArgumentException("Movie with eidrCode " + updateMovieRequest.eidrCode() + " does not exist.");
        }
        if (updateMovieRequest.year() > Year.now().getValue()) {
            throw new IllegalArgumentException("Year cannot be in the future.");
        }
        if (updateMovieRequest.categories() == null || updateMovieRequest.categories().isEmpty()) {
            throw new IllegalArgumentException("Movie has to have at least one category.");
        }
        List<CategoryEntity> categoryEntities = categoryRepository.findCategoriesByIds(updateMovieRequest.categories());
        if (categoryEntities.size() != updateMovieRequest.categories().size()) {
            throw new IllegalArgumentException("Some categories do not exist.");
        }

        // Update movie (if needed)
        movieEntity.setName(updateMovieRequest.name());
        movieEntity.setRating(updateMovieRequest.rating());
        movieEntity.setYear(updateMovieRequest.year());
        movieEntity.setStatus(updateMovieRequest.status());
        if (!currentMovieFieldsEqualRequestedFields(movieEntity, updateMovieRequest)) {
            movieRepository.save(movieEntity);
        }

        // Update categories of that movie (if needed)
        List<CategoryEntity> currentCategories = categoryRepository.findCategoriesByMovieEidrCode(updateMovieRequest.eidrCode());
        if (!currentMovieCategoriesEqualRequestedCategories(currentCategories, updateMovieRequest.categories())) {
            // Remove everything from the bridge table and add the new ones
            List<MovieCategoryEntity> movieCategoryEntities = movieCategoryBridgeRepository.findAllByMovieEidrCodes(List.of(updateMovieRequest.eidrCode()));
            movieCategoryBridgeRepository.deleteAll(movieCategoryEntities);
            movieCategoryBridgeRepository.saveAll(
                    categoryEntities.stream()
                            .map(categoryEntity -> {
                                var MovieCategoryEntityId = new MovieCategoryEntityId();
                                MovieCategoryEntityId.setMovieEidr(updateMovieRequest.eidrCode());
                                MovieCategoryEntityId.setCategoryId(categoryEntity.getId());

                                var movieCategoryEntity = new MovieCategoryEntity();
                                movieCategoryEntity.setId(MovieCategoryEntityId);
                                return movieCategoryEntity;
                            })
                            .toList()
            );
        }

        return convertToMovieResponse(movieEntity, categoryEntities);
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.SERIALIZABLE)
    public boolean deleteMovies(List<String> eidrCodes) {
        // Check if all movies exist
        List<MovieEntity> movieEntities = movieRepository.findAllById(eidrCodes);
        if (movieEntities.size() != eidrCodes.size()) {
            throw new IllegalArgumentException("Some movies do not exist.");
        }

        // Delete from the bridge table
        movieCategoryBridgeRepository.deleteAll(movieCategoryBridgeRepository.findAllByMovieEidrCodes(eidrCodes));

        // Delete from the movie table
        movieRepository.deleteAllById(eidrCodes);

        // Return true if everything went well
        return true;
    }

    // So that there wouldn't be unnecessary updates
    private boolean currentMovieFieldsEqualRequestedFields(MovieEntity movieEntity, UpdateMovieRequest updateMovieRequest) {
        return movieEntity.getEidrCode().equals(updateMovieRequest.eidrCode()) &&
                movieEntity.getName().equals(updateMovieRequest.name()) &&
                movieEntity.getRating().equals(updateMovieRequest.rating()) &&
                movieEntity.getYear().equals(updateMovieRequest.year()) &&
                movieEntity.getStatus().equals(updateMovieRequest.status());
    }

    // So that there wouldn't be unnecessary updates
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
}
