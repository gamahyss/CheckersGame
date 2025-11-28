package test.java;

import main.java.Move;
import org.junit.jupiter.api.Test;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class MoveTest {

    @Test
    public void testMoveCreation() {
        List<Point> captured = Arrays.asList(new Point(2, 3), new Point(4, 5));
        Move move = new Move(5, 6, captured);

        assertEquals(5, move.toCol);
        assertEquals(6, move.toRow);
        assertEquals(2, move.capturedCheckers.size());
        assertEquals(new Point(2, 3), move.capturedCheckers.get(0));
    }

    @Test
    public void testMoveWithEmptyCaptured() {
        List<Point> captured = Arrays.asList();
        Move move = new Move(3, 4, captured);

        assertEquals(3, move.toCol);
        assertEquals(4, move.toRow);
        assertTrue(move.capturedCheckers.isEmpty());
    }
}