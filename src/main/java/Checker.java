package main.java;

import java.awt.*;

public class Checker {
    private Color color;
    private int row;
    private int col;
    private boolean isKing = false;

    public Checker(Color color, int row, int col) {
        this.color = color;
        this.row = row;
        this.col = col;
    }

    public Color getColor() { return color; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public boolean isKing() { return isKing; }
    public void setKing(boolean king) { isKing = king; }
    public void move(int newRow, int newCol) {
        this.row = newRow;
        this.col = newCol;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Checker checker = (Checker) obj;
        return row == checker.row && col == checker.col && color.equals(checker.color);
    }
}