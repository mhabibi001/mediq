package com.quantumx.mediq.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "time_zones") // Renamed table to avoid SQL keyword conflict
@Getter
@Setter
public class TimeZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String timezone;
}
