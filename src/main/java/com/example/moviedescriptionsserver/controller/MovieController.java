package com.example.moviedescriptionsserver.controller;

import com.example.moviedescriptionsserver.dto.*;
import com.example.moviedescriptionsserver.service.MovieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "api/movie")
public class MovieController {

    static Logger logger = LoggerFactory.getLogger(MovieController.class);

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping(value = "/get-movies-table")
    public GetMovieTableResult getMoviesTable(@RequestBody GetMoviesFilter filter) {
        logger.info("Getting all movies");
        return movieService.getAllMovies(filter);
    }

    @PostMapping(value = "/create-movie")
    public GetMovieResponse createMovie(@RequestBody CreateMovieRequest createMovieRequest) {
        logger.info("Creating movie with name: {}", createMovieRequest.name());
        return movieService.createMovie(createMovieRequest);
    }

    @PutMapping(value = "/update-movie")
    public GetMovieResponse updateMovie(@RequestBody UpdateMovieRequest updateMovieRequest) {
        logger.info("Updating movie with eidrCode: {}", updateMovieRequest.eidrCode());
        return movieService.updateMovie(updateMovieRequest);
    }

}
