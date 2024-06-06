package com.example.moviedescriptionsserver.service;

import com.example.moviedescriptionsserver.dto.CreateMovieRequest;
import com.example.moviedescriptionsserver.dto.GetMovieResponse;
import com.example.moviedescriptionsserver.dto.UpdateMovieRequest;
import com.example.moviedescriptionsserver.entity.MovieEntity;
import com.example.moviedescriptionsserver.repository.MovieRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {

    private MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public GetMovieResponse getMovieByEidrCode(String eidrCode) {
        MovieEntity movieEntity = movieRepository.findByEidrCode(eidrCode);
        return convertToMovieResponse(movieEntity);
    }

    public List<GetMovieResponse> getMovieByName(String name) {
        List<MovieEntity> movieEntities = movieRepository.findByName(name);
        return movieEntities.stream().map(this::convertToMovieResponse).toList();
    }

    public GetMovieResponse createMovie(CreateMovieRequest createMovieRequest) {
        return null;
    }

    public GetMovieResponse updateMovie(UpdateMovieRequest updateMovieRequest) {
        return null;
    }

    private GetMovieResponse convertToMovieResponse(MovieEntity movieEntity) {
        return new GetMovieResponse(movieEntity.getEidrCode(), movieEntity.getName(), movieEntity.getRating(), movieEntity.getYear(), movieEntity.getStatus());
    }
}
