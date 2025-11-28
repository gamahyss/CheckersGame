package test.java;

import main.java.Checker;
import org.junit.jupiter.api.Test;
import java.awt.*;
import static org.junit.jupiter.api.Assertions.*;

public class CheckerTest {

    @Test
    public void testCheckerCreation() {
        Checker checker = new Checker(Color.WHITE, 2, 3);

        assertEquals(Color.WHITE, checker.getColor());
        assertEquals(2, checker.getRow());
        assertEquals(3, checker.getCol());
        assertFalse(checker.isKing());
    }

    @Test
    public void testCheckerMove() {
        Checker checker = new Checker(Color.BLACK, 5, 1);
        checker.move(4, 2);

        assertEquals(4, checker.getRow());
        assertEquals(2, checker.getCol());
    }

    @Test
    public void testSetKing() {
        Checker checker = new Checker(Color.WHITE, 0, 1);
        checker.setKing(true);

        assertTrue(checker.isKing());
    }

    @Test
    public void testEquals() {
        Checker checker1 = new Checker(Color.BLACK, 2, 2);
        Checker checker2 = new Checker(Color.BLACK, 2, 2);
        Checker checker3 = new Checker(Color.WHITE, 2, 2);
        Checker checker4 = new Checker(Color.BLACK, 3, 2);

        assertTrue(checker1.equals(checker2));
        assertFalse(checker1.equals(checker3));
        assertFalse(checker1.equals(checker4));
        assertFalse(checker1.equals(null));
        assertTrue(checker1.equals(checker1));
    }
}