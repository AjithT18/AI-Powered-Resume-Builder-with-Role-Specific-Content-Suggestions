package com.eliteresume.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Resume resume;

    private String title;
    private String startMonth;
    private Integer startYear;
    private String endMonth;
    private Integer endYear;
    private boolean present;

    @ElementCollection
    @CollectionTable(name = "project_bullets", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "bullet", columnDefinition = "TEXT")
    @OrderColumn(name = "bullet_order")
    private List<String> bullets = new ArrayList<>();

    private Integer sortOrder;
}
