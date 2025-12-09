package main.java;

import java.io.Serializable;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    private Checker[][] board;
    private int whiteCheckersCount;
    private int blackCheckersCount;
    private boolean isWhiteTurn;

    public GameState(Checker[][] board, int whiteCheckersCount, int blackCheckersCount, boolean isWhiteTurn) {
        this.board = board;
        this.whiteCheckersCount = whiteCheckersCount;
        this.blackCheckersCount = blackCheckersCount;
        this.isWhiteTurn = isWhiteTurn;
    }

    public Checker[][] getBoard() { return board; }
    public int getWhiteCheckersCount() { return whiteCheckersCount; }
    public int getBlackCheckersCount() { return blackCheckersCount; }
    public boolean isWhiteTurn() { return isWhiteTurn; }
}