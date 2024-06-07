package com.example.moviedescriptionsserver.repository;

import com.example.moviedescriptionsserver.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    @Query("SELECT c FROM CategoryEntity c " +
            "JOIN MovieCategoryEntity mc ON c.id = mc.id.categoryId " +
            "JOIN MovieEntity m ON mc.id.movieEidr = m.eidrCode WHERE m.eidrCode = :eidrCode")
    List<CategoryEntity> findCategoriesByMovieEidrCode(String eidrCode);

    @Query("SELECT c FROM CategoryEntity c WHERE c.id IN :ids")
    List<CategoryEntity> findCategoriesByIds(List<Long> ids);

}
