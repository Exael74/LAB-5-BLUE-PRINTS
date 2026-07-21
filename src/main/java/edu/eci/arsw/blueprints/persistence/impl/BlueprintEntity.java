package edu.eci.arsw.blueprints.persistence.impl;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que mapea un blueprint a la tabla {@code blueprints}.
 * Los puntos se guardan como colección ordenada en la tabla {@code blueprint_points}.
 */
@Entity
@Table(name = "blueprints",
        uniqueConstraints = @UniqueConstraint(columnNames = {"author", "name"}))
public class BlueprintEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "blueprint_points", joinColumns = @JoinColumn(name = "blueprint_id"))
    @OrderColumn(name = "position")
    private List<PointEmbeddable> points = new ArrayList<>();

    protected BlueprintEntity() { }

    public BlueprintEntity(String author, String name, List<PointEmbeddable> points) {
        this.author = author;
        this.name = name;
        if (points != null) this.points = points;
    }

    public Long getId() { return id; }
    public String getAuthor() { return author; }
    public String getName() { return name; }
    public List<PointEmbeddable> getPoints() { return points; }

    public void setPoints(List<PointEmbeddable> points) { this.points = points; }
}
