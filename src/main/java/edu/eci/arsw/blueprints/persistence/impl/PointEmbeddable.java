package edu.eci.arsw.blueprints.persistence.impl;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Punto embebido dentro de la colección de puntos de un {@link BlueprintEntity}. */
@Embeddable
public class PointEmbeddable {

    @Column(name = "x", nullable = false)
    private int x;

    @Column(name = "y", nullable = false)
    private int y;

    protected PointEmbeddable() { }

    public PointEmbeddable(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }
}
