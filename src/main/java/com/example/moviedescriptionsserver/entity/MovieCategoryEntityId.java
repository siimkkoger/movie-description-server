package com.example.moviedescriptionsserver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class MovieCategoryEntityId implements Serializable {

    @Column(name = "movie_eidr", nullable = false, updatable = false)
    private String movieEidr;
    @Column(name = "category_id", nullable = false, updatable = false)
    private Long categoryId;

    public MovieCategoryEntityId(String movieEidr, Long categoryId) {
        this.movieEidr = movieEidr;
        this.categoryId = categoryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovieCategoryEntityId that = (MovieCategoryEntityId) o;
        return Objects.equals(movieEidr, that.movieEidr) && Objects.equals(categoryId, that.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movieEidr, categoryId);
    }

}
