package com.example.moviedescriptionsserver.entity;

import com.example.moviedescriptionsserver.MovieStatus;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Table(name = "movies", schema = "public")
@Getter
@Setter
public class MovieEntity {

    @Id
    @Column(name = "eidr_code", nullable = false, updatable = false)
    private String eidrCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "rating", nullable = false)
    private Double rating;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Enumerated(STRING)
    @Column(name = "status", nullable = false)
    private MovieStatus status;

}
