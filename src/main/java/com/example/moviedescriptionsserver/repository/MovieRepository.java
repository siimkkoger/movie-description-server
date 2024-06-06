package com.example.moviedescriptionsserver.repository;

import com.example.moviedescriptionsserver.entity.MovieEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<MovieEntity, String> {

    MovieEntity findByEidrCode(String eidrCode);
}
