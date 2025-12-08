package main.java;

import java.awt.*;
import java.util.List;

public class Move {
    public int toCol;
    public int toRow;
    public java.util.List<Point> capturedCheckers;

    public Move(int toCol, int toRow, List<Point> capturedCheckers) {
        this.toCol = toCol;
        this.toRow = toRow;
        this.capturedCheckers = capturedCheckers;
    }
}
