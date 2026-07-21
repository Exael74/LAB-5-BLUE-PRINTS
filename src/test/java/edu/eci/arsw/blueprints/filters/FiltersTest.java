package edu.eci.arsw.blueprints.filters;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FiltersTest {

    @Test
    void redundancyRemovesConsecutiveDuplicates() {
        Blueprint bp = new Blueprint("john", "house", List.of(
                new Point(0, 0), new Point(0, 0), new Point(1, 1), new Point(1, 1), new Point(2, 2)));
        Blueprint out = new RedundancyFilter().apply(bp);
        assertEquals(List.of(new Point(0, 0), new Point(1, 1), new Point(2, 2)), out.getPoints());
    }

    @Test
    void undersamplingKeepsEveryOtherPoint() {
        Blueprint bp = new Blueprint("john", "house", List.of(
                new Point(0, 0), new Point(1, 1), new Point(2, 2), new Point(3, 3)));
        Blueprint out = new UndersamplingFilter().apply(bp);
        assertEquals(List.of(new Point(0, 0), new Point(2, 2)), out.getPoints());
    }

    @Test
    void identityReturnsSameContent() {
        Blueprint bp = new Blueprint("john", "house", List.of(new Point(0, 0), new Point(1, 1)));
        Blueprint out = new IdentityFilter().apply(bp);
        assertEquals(bp.getPoints(), out.getPoints());
    }
}
