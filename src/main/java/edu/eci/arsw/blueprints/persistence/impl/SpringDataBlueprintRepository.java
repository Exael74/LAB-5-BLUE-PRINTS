package edu.eci.arsw.blueprints.persistence.impl;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Repositorio Spring Data JPA para {@link BlueprintEntity}. */
public interface SpringDataBlueprintRepository extends JpaRepository<BlueprintEntity, Long> {

    Optional<BlueprintEntity> findByAuthorAndName(String author, String name);

    List<BlueprintEntity> findByAuthor(String author);

    boolean existsByAuthorAndName(String author, String name);
}
