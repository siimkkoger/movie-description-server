package com.example.moviedescriptionsserver.entity;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

@Entity
@Table(name = "movie", schema = "public")
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

    @Column(name = "status", nullable = false)
    private Boolean status;

}
