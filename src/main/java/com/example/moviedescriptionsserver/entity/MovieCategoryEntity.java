package com.example.moviedescriptionsserver.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "movie_category", schema = "public")
public class MovieCategoryEntity {

    @EmbeddedId
    private MovieCategoryEntityId id;

}
