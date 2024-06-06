package com.example.moviedescriptionsserver.repository;

import com.example.moviedescriptionsserver.entity.MovieCategoryEntity;
import com.example.moviedescriptionsserver.entity.MovieCategoryEntityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieCategoryBridgeRepository extends JpaRepository<MovieCategoryEntity, MovieCategoryEntityId> {

    @Query("SELECT mce FROM MovieCategoryEntity mce WHERE mce.id.movieEidr in :eidrCodes")
    List<MovieCategoryEntity> findAllByMovieEidrCodes(List<String> eidrCodes);

}
