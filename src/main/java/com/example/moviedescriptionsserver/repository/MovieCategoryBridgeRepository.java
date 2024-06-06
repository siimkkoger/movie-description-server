package com.example.moviedescriptionsserver.repository;

import com.example.moviedescriptionsserver.entity.MovieCategoryEntity;
import com.example.moviedescriptionsserver.entity.MovieCategoryEntityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieCategoryBridgeRepository extends JpaRepository<MovieCategoryEntity, MovieCategoryEntityId> {

}
