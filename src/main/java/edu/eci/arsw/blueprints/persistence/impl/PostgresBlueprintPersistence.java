package edu.eci.arsw.blueprints.persistence.impl;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistence;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Persistencia en PostgreSQL (Actividad 2). Respeta el contrato de
 * {@link BlueprintPersistence} y se activa solo con el perfil "postgres".
 */
@Repository
@Profile("postgres")
@Transactional
public class PostgresBlueprintPersistence implements BlueprintPersistence {

    private final SpringDataBlueprintRepository repo;

    public PostgresBlueprintPersistence(SpringDataBlueprintRepository repo) {
        this.repo = repo;
    }

    @Override
    public void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException {
        if (repo.existsByAuthorAndName(bp.getAuthor(), bp.getName())) {
            throw new BlueprintPersistenceException(
                    "Blueprint already exists: " + bp.getAuthor() + ":" + bp.getName());
        }
        repo.save(toEntity(bp));
    }

    @Override
    @Transactional(readOnly = true)
    public Blueprint getBlueprint(String author, String name) throws BlueprintNotFoundException {
        return toDomain(find(author, name));
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException {
        List<BlueprintEntity> list = repo.findByAuthor(author);
        if (list.isEmpty()) {
            throw new BlueprintNotFoundException("No blueprints for author: " + author);
        }
        return list.stream().map(this::toDomain).collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Blueprint> getAllBlueprints() {
        return repo.findAll().stream().map(this::toDomain).collect(Collectors.toSet());
    }

    @Override
    public void addPoint(String author, String name, int x, int y) throws BlueprintNotFoundException {
        BlueprintEntity e = find(author, name);
        e.getPoints().add(new PointEmbeddable(x, y));
        repo.save(e);
    }

    @Override
    public void deleteBlueprint(String author, String name) throws BlueprintNotFoundException {
        repo.delete(find(author, name));
    }

    // --- helpers de mapeo ---

    private BlueprintEntity find(String author, String name) throws BlueprintNotFoundException {
        return repo.findByAuthorAndName(author, name)
                .orElseThrow(() -> new BlueprintNotFoundException(
                        "Blueprint not found: %s/%s".formatted(author, name)));
    }

    private BlueprintEntity toEntity(Blueprint bp) {
        List<PointEmbeddable> pts = bp.getPoints().stream()
                .map(p -> new PointEmbeddable(p.x(), p.y()))
                .collect(Collectors.toList());
        return new BlueprintEntity(bp.getAuthor(), bp.getName(), pts);
    }

    private Blueprint toDomain(BlueprintEntity e) {
        List<Point> pts = e.getPoints().stream()
                .map(p -> new Point(p.getX(), p.getY()))
                .collect(Collectors.toList());
        return new Blueprint(e.getAuthor(), e.getName(), pts);
    }
}
