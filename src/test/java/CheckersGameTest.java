package test.java;
import main.java.Checker;
import main.java.CheckersGame;
import main.java.Move;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CheckersGameTest {

    private CheckersGame game;
    private Checker[][] board;

    @BeforeEach
    public void setUp() throws Exception {
        game = new CheckersGame();

        Field boardField = CheckersGame.class.getDeclaredField("board");
        boardField.setAccessible(true);
        board = (Checker[][]) boardField.get(game);
    }

    @Test
    public void testInitialBoardSetup() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 8; col++) {
                if ((row + col) % 2 == 1) {
                    assertNotNull(board[row][col], "Black checker should be at (" + row + "," + col + ")");
                    assertEquals(Color.BLACK, board[row][col].getColor());
                    assertFalse(board[row][col].isKing());
                } else {
                    assertNull(board[row][col], "No checker should be at (" + row + "," + col + ")");
                }
            }
        }

        for (int row = 5; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if ((row + col) % 2 == 1) {
                    assertNotNull(board[row][col], "White checker should be at (" + row + "," + col + ")");
                    assertEquals(Color.WHITE, board[row][col].getColor());
                    assertFalse(board[row][col].isKing());
                } else {
                    assertNull(board[row][col], "No checker should be at (" + row + "," + col + ")");
                }
            }
        }

        for (int row = 3; row < 5; row++) {
            for (int col = 0; col < 8; col++) {
                assertNull(board[row][col], "Middle should be empty at (" + row + "," + col + ")");
            }
        }
    }

    @Test
    public void testCheckerMovement() throws Exception {
        clearBoard();

        Checker whiteChecker = new Checker(Color.WHITE, 5, 1);
        board[5][1] = whiteChecker;

        Method calculateValidMoves = CheckersGame.class.getDeclaredMethod("calculateValidMoves", Checker.class);
        calculateValidMoves.setAccessible(true);
        calculateValidMoves.invoke(game, whiteChecker);

        Field validMovesField = CheckersGame.class.getDeclaredField("validMoves");
        validMovesField.setAccessible(true);
        ArrayList<Move> validMoves = (ArrayList<Move>) validMovesField.get(game);

        assertTrue(validMoves.size() > 0, "White checker should have valid moves");
    }

    @Test
    public void testCaptureMove() throws Exception {
        clearBoard();

        Checker whiteChecker = new Checker(Color.WHITE, 4, 4);
        Checker blackChecker = new Checker(Color.BLACK, 3, 3);

        board[4][4] = whiteChecker;
        board[3][3] = blackChecker;

        Method calculateValidMoves = CheckersGame.class.getDeclaredMethod("calculateValidMoves", Checker.class);
        calculateValidMoves.setAccessible(true);
        calculateValidMoves.invoke(game, whiteChecker);

        Field validMovesField = CheckersGame.class.getDeclaredField("validMoves");
        validMovesField.setAccessible(true);
        ArrayList<Move> validMoves = (ArrayList<Move>) validMovesField.get(game);

        boolean hasCapture = false;
        for (Move move : validMoves) {
            if (!move.capturedCheckers.isEmpty()) {
                hasCapture = true;
                break;
            }
        }
        assertTrue(hasCapture, "Should have capture moves available");
    }

    @Test
    public void testMultipleCapture() throws Exception {
        clearBoard();

        Checker whiteChecker = new Checker(Color.WHITE, 4, 4);
        Checker blackChecker1 = new Checker(Color.BLACK, 3, 3);
        Checker blackChecker2 = new Checker(Color.BLACK, 1, 1);

        board[4][4] = whiteChecker;
        board[3][3] = blackChecker1;
        board[1][1] = blackChecker2;

        whiteChecker.setKing(true);

        Method calculateValidMoves = CheckersGame.class.getDeclaredMethod("calculateValidMoves", Checker.class);
        calculateValidMoves.setAccessible(true);
        calculateValidMoves.invoke(game, whiteChecker);

        Field validMovesField = CheckersGame.class.getDeclaredField("validMoves");
        validMovesField.setAccessible(true);
        ArrayList<Move> validMoves = (ArrayList<Move>) validMovesField.get(game);

        boolean hasMultipleCapture = false;
        for (Move move : validMoves) {
            if (move.capturedCheckers.size() > 1) {
                hasMultipleCapture = true;
                break;
            }
        }
        assertTrue(hasMultipleCapture, "King should have multiple capture moves");
    }

    @Test
    public void testTurnSwitching() throws Exception {
        Field isWhiteTurnField = CheckersGame.class.getDeclaredField("isWhiteTurn");
        isWhiteTurnField.setAccessible(true);
        boolean initialTurn = (boolean) isWhiteTurnField.get(game);
        assertTrue(initialTurn, "Initial turn should be white");

        Field mustContinueCaptureField = CheckersGame.class.getDeclaredField("mustContinueCapture");
        mustContinueCaptureField.setAccessible(true);
        mustContinueCaptureField.set(game, false);

        isWhiteTurnField.set(game, false);
        boolean afterTurn = (boolean) isWhiteTurnField.get(game);
        assertFalse(afterTurn, "Turn should switch to black");
    }

    @Test
    public void testWinCondition() throws Exception {
        clearBoard();

        Checker whiteChecker = new Checker(Color.WHITE, 0, 1);
        board[0][1] = whiteChecker;

        Field whiteCountField = CheckersGame.class.getDeclaredField("whiteCheckersCount");
        Field blackCountField = CheckersGame.class.getDeclaredField("blackCheckersCount");
        whiteCountField.setAccessible(true);
        blackCountField.setAccessible(true);

        whiteCountField.set(game, 1);
        blackCountField.set(game, 0);

        Method updateStatus = CheckersGame.class.getDeclaredMethod("updateStatus");
        updateStatus.setAccessible(true);

        assertDoesNotThrow(() -> updateStatus.invoke(game));
    }

    @Test
    public void testBoardCopy() throws Exception {
        clearBoard();
        board[0][1] = new Checker(Color.BLACK, 0, 1);
        board[7][6] = new Checker(Color.WHITE, 7, 6);

        Method copyBoard = CheckersGame.class.getDeclaredMethod("copyBoard");
        copyBoard.setAccessible(true);
        Checker[][] copy = (Checker[][]) copyBoard.invoke(game);

        assertNotNull(copy[0][1]);
        assertEquals(Color.BLACK, copy[0][1].getColor());
        assertNotNull(copy[7][6]);
        assertEquals(Color.WHITE, copy[7][6].getColor());

        assertNotSame(board[0][1], copy[0][1]);
        assertNotSame(board[7][6], copy[7][6]);
    }

    private void clearBoard() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = null;
            }
        }
    }

    @Test
    public void testMoveClass() {
        List<Point> captured = new ArrayList<>();
        captured.add(new Point(2, 3));
        captured.add(new Point(4, 5));

        Move move = new Move(5, 6, captured);

        assertEquals(5, move.toCol);
        assertEquals(6, move.toRow);
        assertEquals(2, move.capturedCheckers.size());
        assertEquals(new Point(2, 3), move.capturedCheckers.get(0));
    }

    @Test
    public void testCheckerClass() {
        Checker checker = new Checker(Color.WHITE, 2, 3);

        assertEquals(Color.WHITE, checker.getColor());
        assertEquals(2, checker.getRow());
        assertEquals(3, checker.getCol());
        assertFalse(checker.isKing());

        checker.move(4, 5);
        assertEquals(4, checker.getRow());
        assertEquals(5, checker.getCol());

        checker.setKing(true);
        assertTrue(checker.isKing());

        Checker sameChecker = new Checker(Color.WHITE, 4, 5);
        Checker differentChecker = new Checker(Color.BLACK, 4, 5);

        assertTrue(checker.equals(sameChecker));
        assertFalse(checker.equals(differentChecker));
        assertFalse(checker.equals(null));
        assertTrue(checker.equals(checker));
    }
}