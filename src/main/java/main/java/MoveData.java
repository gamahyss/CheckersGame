package main.java;

import java.io.Serializable;
import java.util.List;
import java.awt.Point;

public class MoveData implements Serializable {
    private static final long serialVersionUID = 1L;

    private int fromRow;
    private int fromCol;
    private int toRow;
    private int toCol;
    private List<Point> captured;

    public MoveData(int fromRow, int fromCol, int toRow, int toCol, List<Point> captured) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.captured = captured;
    }

    public int getFromRow() { return fromRow; }
    public int getFromCol() { return fromCol; }
    public int getToRow() { return toRow; }
    public int getToCol() { return toCol; }
    public List<Point> getCaptured() { return captured; }
}