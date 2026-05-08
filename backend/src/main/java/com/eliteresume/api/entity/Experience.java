package com.eliteresume.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "experiences")
public class Experience {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Resume resume;

    private String role;
    private String organization;
    private String startMonth;
    private Integer startYear;
    private String endMonth;
    private Integer endYear;
    private boolean present;

    @ElementCollection
    @CollectionTable(name = "experience_bullets", joinColumns = @JoinColumn(name = "experience_id"))
    @Column(name = "bullet", columnDefinition = "TEXT")
    @OrderColumn(name = "bullet_order")
    private List<String> bullets = new ArrayList<>();

    private Integer sortOrder;
}
