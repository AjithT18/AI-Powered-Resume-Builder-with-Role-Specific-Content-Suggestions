package com.eliteresume.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "education")
public class Education {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Resume resume;

    private String school;
    private String degree;
    private Integer startYear;
    private Integer endYear;

    @Enumerated(EnumType.STRING)
    private ScoreType scoreType;

    private String score;
    private Integer sortOrder;
}
