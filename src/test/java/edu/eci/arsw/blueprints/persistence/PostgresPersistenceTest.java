package edu.eci.arsw.blueprints.persistence;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ejercita {@code PostgresBlueprintPersistence} contra una base H2 en modo PostgreSQL,
 * de modo que las pruebas no dependen de un contenedor real.
 */
@SpringBootTest
@ActiveProfiles("postgres")
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=",
        "spring.datasource.url=jdbc:h2:mem:blueprints;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class PostgresPersistenceTest {

    @Autowired
    private BlueprintPersistence persistence;

    @Test
    void usesPostgresImplementation() {
        // el bean puede venir envuelto en un proxy CGLIB por @Transactional
        assertTrue(persistence.getClass().getName().contains("PostgresBlueprintPersistence"),
                "Se esperaba la implementacion PostgreSQL, pero fue: " + persistence.getClass().getName());
    }

    @Test
    void savesAndReadsBlueprintPreservingOrder() throws Exception {
        persistence.saveBlueprint(new Blueprint("ana", "kitchen",
                List.of(new Point(1, 1), new Point(2, 2), new Point(3, 3))));

        Blueprint bp = persistence.getBlueprint("ana", "kitchen");
        assertEquals(List.of(new Point(1, 1), new Point(2, 2), new Point(3, 3)), bp.getPoints());
    }

    @Test
    void rejectsDuplicateAndSupportsAddPointAndDelete() throws Exception {
        persistence.saveBlueprint(new Blueprint("bob", "shed", List.of(new Point(0, 0))));

        assertThrows(BlueprintPersistenceException.class,
                () -> persistence.saveBlueprint(new Blueprint("bob", "shed", List.of())));

        persistence.addPoint("bob", "shed", 5, 5);
        assertEquals(2, persistence.getBlueprint("bob", "shed").getPoints().size());

        persistence.deleteBlueprint("bob", "shed");
        assertThrows(BlueprintNotFoundException.class, () -> persistence.getBlueprint("bob", "shed"));
    }
}
