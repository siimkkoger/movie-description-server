package com.example.moviedescriptionsserver.controller;

import com.example.moviedescriptionsserver.dto.CreateMovieRequest;
import com.example.moviedescriptionsserver.dto.MovieResponse;
import com.example.moviedescriptionsserver.dto.UpdateMovieRequest;
import com.example.moviedescriptionsserver.service.MovieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/movie")
public class MovieController {

    static Logger logger = LoggerFactory.getLogger(MovieController.class);

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping(value = "/{eidrCode}")
    public MovieResponse getMovieByEidrCode(@PathVariable String eidrCode) {
        logger.info("Getting movie with eidrCode: {}", eidrCode);
        return movieService.getMovieByEidrCode(eidrCode);
    }

    @GetMapping(value = "/{name}")
    public MovieResponse getMovieByName(@PathVariable String name) {
        logger.info("Getting movie with name: {}", name);
        return movieService.getMovieByName(name);
    }

    @PostMapping
    public MovieResponse createMovie(@RequestBody CreateMovieRequest createMovieRequest) {
        logger.info("Creating movie with name: {}", createMovieRequest.name());
        return movieService.createMovie(createMovieRequest);
    }

    @PutMapping
    public MovieResponse updateMovie(@RequestBody UpdateMovieRequest updateMovieRequest) {
        logger.info("Updating movie with eidrCode: {}", updateMovieRequest.eidrCode());
        return movieService.updateMovie(updateMovieRequest);
    }

}
