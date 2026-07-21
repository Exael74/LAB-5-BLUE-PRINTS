package edu.eci.arsw.blueprints.services;

import edu.eci.arsw.blueprints.filters.IdentityFilter;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.persistence.InMemoryBlueprintPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BlueprintsServicesTest {

    private BlueprintsServices services;

    @BeforeEach
    void setUp() {
        services = new BlueprintsServices(new InMemoryBlueprintPersistence(), new IdentityFilter());
    }

    @Test
    void createsAndRetrievesBlueprint() throws Exception {
        services.addNewBlueprint(new Blueprint("ana", "kitchen", List.of(new Point(1, 1))));
        Blueprint bp = services.getBlueprint("ana", "kitchen");
        assertEquals("ana", bp.getAuthor());
        assertEquals(1, bp.getPoints().size());
    }

    @Test
    void rejectsDuplicateBlueprint() throws Exception {
        services.addNewBlueprint(new Blueprint("ana", "kitchen", List.of()));
        assertThrows(BlueprintPersistenceException.class,
                () -> services.addNewBlueprint(new Blueprint("ana", "kitchen", List.of())));
    }

    @Test
    void addPointGrowsTheBlueprint() throws Exception {
        services.addPoint("john", "house", 99, 99);
        assertEquals(5, services.getBlueprint("john", "house").getPoints().size());
    }

    @Test
    void totalPointsByAuthorSumsAll() throws Exception {
        // seed: john/house(4) + john/garage(3) = 7
        assertEquals(7, services.totalPointsByAuthor("john"));
    }

    @Test
    void deleteRemovesBlueprint() throws Exception {
        services.deleteBlueprint("jane", "garden");
        assertThrows(BlueprintNotFoundException.class, () -> services.getBlueprint("jane", "garden"));
    }

    @Test
    void missingBlueprintThrows() {
        assertThrows(BlueprintNotFoundException.class, () -> services.getBlueprint("nobody", "nothing"));
    }
}
