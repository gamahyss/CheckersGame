package main.java;

import main.java.Checker;
import main.java.Move;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class CheckersGame extends JFrame {
    private final int BOARD_SIZE = 8;
    private int tileSize;
    private Checker[][] board = new Checker[BOARD_SIZE][BOARD_SIZE];
    private Checker selectedChecker = null;
    private ArrayList<Move> validMoves = new ArrayList<>();
    private boolean isWhiteTurn = true;
    private boolean mustContinueCapture = false;
    private Checker checkerInCaptureSequence = null;
    private int whiteCheckersCount = 12;
    private int blackCheckersCount = 12;
    private JLabel statusLabel;
    private boolean hasMadeMove = false;
    private boolean fullScreenMode = false;
    private CheckersPanel gamePanel;

    public CheckersGame() {
        setTitle("Шашки");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gamePanel = new CheckersPanel();
        add(gamePanel);

        statusLabel = new JLabel("Ход белых", SwingConstants.CENTER);
        statusLabel.setPreferredSize(new Dimension(800, 30));
        add(statusLabel, BorderLayout.SOUTH);

        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("");
        JMenuItem fullScreenItem = new JMenuItem("Полноэкранный режим");
        JMenuItem exitItem = new JMenuItem("Выход");

        fullScreenItem.addActionListener(e -> toggleFullScreen());
        exitItem.addActionListener(e -> System.exit(0));

        gameMenu.add(fullScreenItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);
        menuBar.add(gameMenu);
        setJMenuBar(menuBar);

        initializeBoard();
        updateStatus();

        setPreferredSize(new Dimension(800, 800));
        pack();
        setLocationRelativeTo(null);
    }

    private void initializeBoard() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if ((row + col) % 2 == 1) {
                    board[row][col] = new Checker(Color.BLACK, row, col);
                }
            }
        }

        for (int row = 5; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if ((row + col) % 2 == 1) {
                    board[row][col] = new Checker(Color.WHITE, row, col);
                }
            }
        }
    }

    private void toggleFullScreen() {
        fullScreenMode = !fullScreenMode;

        if (fullScreenMode) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            setUndecorated(true);
        } else {
            setExtendedState(JFrame.NORMAL);
            setUndecorated(false);
            pack();
            setLocationRelativeTo(null);
        }

        gamePanel.recalculateTileSize();
        repaint();
    }

    private void updateStatus() {
        String status = isWhiteTurn ? "Ход белых" : "Ход черных";
        if (mustContinueCapture) {
            status += " (продолжите взятие)";
        }
        status += " | Белые: " + whiteCheckersCount + " | Черные: " + blackCheckersCount;
        if (fullScreenMode) {
            status += " | F11 - выход из полноэкранного режима";
        }
        statusLabel.setText(status);

        if (whiteCheckersCount == 0) {
            JOptionPane.showMessageDialog(this, "Черные выиграли!");
            resetGame();
        } else if (blackCheckersCount == 0) {
            JOptionPane.showMessageDialog(this, "Белые выиграли!");
            resetGame();
        }
    }

    private void resetGame() {
        board = new Checker[BOARD_SIZE][BOARD_SIZE];
        whiteCheckersCount = blackCheckersCount = 12;
        isWhiteTurn = true;
        mustContinueCapture = false;
        selectedChecker = null;
        validMoves.clear();
        hasMadeMove = false;
        initializeBoard();
        gamePanel.repaint();
        updateStatus();
    }

    public void makeMove(Checker whiteChecker, int i, int i1, ArrayList<Object> objects) {
    }

    public class CheckersPanel extends JPanel {
        private int boardWidth, boardHeight;
        private int boardX, boardY;

        public CheckersPanel() {
            setBackground(Color.DARK_GRAY);
            addMouseListener(new ClickListener());
        }

        public void recalculateTileSize() {
            int width = getWidth();
            int height = getHeight();

            int boardSize = (int) (Math.min(width, height) * 0.9);
            tileSize = boardSize / BOARD_SIZE;

            boardX = (width - boardSize) / 2;
            boardY = (height - boardSize) / 2;
            boardWidth = boardHeight = boardSize;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            recalculateTileSize();
            drawBoard(g);
            drawCheckers(g);
            highlightValidMoves(g);
        }

        private void drawBoard(Graphics g) {
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    int x = boardX + col * tileSize;
                    int y = boardY + row * tileSize;

                    if ((row + col) % 2 == 0) {
                        g.setColor(new Color(240, 217, 181));
                    } else {
                        g.setColor(new Color(181, 136, 99));
                    }
                    g.fillRect(x, y, tileSize, tileSize);
                }
            }
        }

        private void drawCheckers(Graphics g) {
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    if (board[row][col] != null) {
                        Checker checker = board[row][col];
                        int x = boardX + col * tileSize;
                        int y = boardY + row * tileSize;

                        if (checker == selectedChecker) {
                            g.setColor(Color.YELLOW);
                            g.fillOval(x + 2, y + 2, tileSize - 4, tileSize - 4);
                        }

                        g.setColor(checker.getColor());
                        g.fillOval(x + 5, y + 5, tileSize - 10, tileSize - 10);

                        g.setColor(checker.getColor().darker());
                        g.drawOval(x + 5, y + 5, tileSize - 10, tileSize - 10);

                        if (checker.isKing()) {
                            g.setColor(Color.YELLOW);
                            int crownSize = tileSize / 3;
                            g.fillPolygon(new int[]{
                                    x + tileSize/2 - crownSize,
                                    x + tileSize/2,
                                    x + tileSize/2 + crownSize
                            }, new int[]{
                                    y + tileSize/2 + crownSize/2,
                                    y + tileSize/2 - crownSize,
                                    y + tileSize/2 + crownSize/2
                            }, 3);
                        }
                    }
                }
            }
        }

        private void highlightValidMoves(Graphics g) {
            g.setColor(Color.GREEN);
            for (Move move : validMoves) {
                int x = boardX + move.toCol * tileSize;
                int y = boardY + move.toRow * tileSize;
                g.drawRect(x, y, tileSize, tileSize);

                if (!move.capturedCheckers.isEmpty()) {
                    g.setColor(Color.RED);
                    g.fillOval(x + tileSize/2 - 5, y + tileSize/2 - 5, 10, 10);
                    g.setColor(Color.GREEN);
                }
            }
        }

        private Point screenToBoard(int screenX, int screenY) {
            int col = (screenX - boardX) / tileSize;
            int row = (screenY - boardY) / tileSize;

            if (col < 0 || col >= BOARD_SIZE || row < 0 || row >= BOARD_SIZE) {
                return null;
            }
            return new Point(col, row);
        }

        private class ClickListener extends MouseAdapter {
            @Override
            public void mousePressed(MouseEvent e) {
                Point boardPos = screenToBoard(e.getX(), e.getY());
                if (boardPos == null) return;

                int col = boardPos.x;
                int row = boardPos.y;

                if (mustContinueCapture && selectedChecker != null &&
                        (board[row][col] == null || board[row][col] != selectedChecker)) {

                    for (Move move : validMoves) {
                        if (move.toCol == col && move.toRow == row && !move.capturedCheckers.isEmpty()) {
                            makeMove(selectedChecker, col, row, move.capturedCheckers);
                            return;
                        }
                    }
                    return;
                }

                if (board[row][col] != null && board[row][col].getColor() == (isWhiteTurn ? Color.WHITE : Color.BLACK)) {
                    if (selectedChecker != null && selectedChecker != board[row][col]) {
                        selectedChecker = board[row][col];
                        calculateValidMoves(selectedChecker);

                        if (hasMandatoryCapture() && !hasCaptureMoves()) {
                            selectedChecker = null;
                            validMoves.clear();
                        }

                        repaint();
                        return;
                    }

                    if (selectedChecker == null) {
                        selectedChecker = board[row][col];
                        calculateValidMoves(selectedChecker);

                        if (hasMandatoryCapture() && !hasCaptureMoves()) {
                            selectedChecker = null;
                            validMoves.clear();
                        }

                        repaint();
                    }
                } else if (selectedChecker != null) {
                    for (Move move : validMoves) {
                        if (move.toCol == col && move.toRow == row) {
                            makeMove(selectedChecker, col, row, move.capturedCheckers);
                            break;
                        }
                    }
                }
            }

            private boolean hasMandatoryCapture() {
                Color currentColor = isWhiteTurn ? Color.WHITE : Color.BLACK;
                for (int row = 0; row < BOARD_SIZE; row++) {
                    for (int col = 0; col < BOARD_SIZE; col++) {
                        if (board[row][col] != null && board[row][col].getColor() == currentColor) {
                            Checker checker = board[row][col];
                            ArrayList<Move> moves = new ArrayList<>();

                            ArrayList<Move> temp = new ArrayList<>(validMoves);
                            calculateValidMoves(checker);
                            moves.addAll(validMoves);
                            validMoves.clear();
                            validMoves.addAll(temp);

                            if (moves.stream().anyMatch(m -> !m.capturedCheckers.isEmpty())) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }

            private void makeMove(Checker checker, int newCol, int newRow, List<Point> capturedCheckers) {
                for (Point captured : capturedCheckers) {
                    if (board[captured.y][captured.x] != null) {
                        if (board[captured.y][captured.x].getColor() == Color.WHITE) {
                            whiteCheckersCount--;
                        } else {
                            blackCheckersCount--;
                        }
                        board[captured.y][captured.x] = null;
                    }
                }

                board[checker.getRow()][checker.getCol()] = null;
                checker.move(newRow, newCol);
                board[newRow][newCol] = checker;

                if (!checker.isKing()) {
                    if ((checker.getColor() == Color.WHITE && newRow == 0) ||
                            (checker.getColor() == Color.BLACK && newRow == BOARD_SIZE - 1)) {
                        checker.setKing(true);
                    }
                }

                if (!capturedCheckers.isEmpty()) {
                    calculateValidMoves(checker);
                    if (hasCaptureMoves()) {
                        mustContinueCapture = true;
                        checkerInCaptureSequence = checker;
                        selectedChecker = checker;
                        hasMadeMove = true;
                        updateStatus();
                        repaint();
                        return;
                    }
                }

                mustContinueCapture = false;
                checkerInCaptureSequence = null;
                selectedChecker = null;
                validMoves.clear();
                isWhiteTurn = !isWhiteTurn;
                hasMadeMove = false;
                updateStatus();
                repaint();
            }
        }
    }

    public void calculateValidMoves(Checker checker) {
        validMoves.clear();

        if (mustContinueCapture && checker != checkerInCaptureSequence) {
            return;
        }

        if (checker.isKing()) {
            calculateKingMoves(checker);
        } else {
            calculateNormalMoves(checker);
        }

        // Если есть взятия, убираем простые ходы
        if (hasCaptureMoves()) {
            validMoves.removeIf(move -> move.capturedCheckers.isEmpty());
        }
    }

    private void calculateNormalMoves(Checker checker) {
        int x = checker.getCol();
        int y = checker.getRow();
        Color color = checker.getColor();
        int direction = (color == Color.WHITE) ? -1 : 1;

        // Простые ходы
        if (!mustContinueCapture) {
            checkNormalMove(x - 1, y + direction, color);
            checkNormalMove(x + 1, y + direction, color);
        }

        checkNormalCapture(x, y, color, new ArrayList<>());
    }

    private void checkNormalMove(int x, int y, Color color) {
        if (x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE && board[y][x] == null) {
            validMoves.add(new Move(x, y, new ArrayList<>()));
        }
    }

    private void checkNormalCapture(int x, int y, Color color, List<Point> capturedSoFar) {
        int[][] captureDirections = {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}};

        for (int[] dir : captureDirections) {
            int enemyX = x + dir[0];
            int enemyY = y + dir[1];
            int targetX = enemyX + dir[0];
            int targetY = enemyY + dir[1];

            if (isValidCapture(x, y, enemyX, enemyY, targetX, targetY, color, capturedSoFar)) {
                List<Point> newCaptured = new ArrayList<>(capturedSoFar);
                newCaptured.add(new Point(enemyX, enemyY));

                Checker[][] tempBoard = copyBoard();
                tempBoard[enemyY][enemyX] = null; // Убираем взятую шашку
                tempBoard[y][x] = null; // Убираем текущую шашку
                tempBoard[targetY][targetX] = new Checker(color, targetY, targetX);

                checkMultipleCaptures(targetX, targetY, color, newCaptured, tempBoard);
            }
        }
    }

    private void checkMultipleCaptures(int x, int y, Color color, List<Point> capturedSoFar, Checker[][] tempBoard) {
        boolean foundAdditionalCapture = false;
        int[][] captureDirections = {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}};

        for (int[] dir : captureDirections) {
            int enemyX = x + dir[0];
            int enemyY = y + dir[1];
            int targetX = enemyX + dir[0];
            int targetY = enemyY + dir[1];

            if (targetX >= 0 && targetX < BOARD_SIZE && targetY >= 0 && targetY < BOARD_SIZE &&
                    tempBoard[enemyY][enemyX] != null && tempBoard[enemyY][enemyX].getColor() != color &&
                    tempBoard[targetY][targetX] == null && !isAlreadyCaptured(enemyX, enemyY, capturedSoFar)) {

                List<Point> newCaptured = new ArrayList<>(capturedSoFar);
                newCaptured.add(new Point(enemyX, enemyY));
                foundAdditionalCapture = true;

                Checker[][] newTempBoard = copyBoard(tempBoard);
                newTempBoard[enemyY][enemyX] = null;
                newTempBoard[y][x] = null;
                newTempBoard[targetY][targetX] = new Checker(color, targetY, targetX);

                checkMultipleCaptures(targetX, targetY, color, newCaptured, newTempBoard);
            }
        }

        if (!foundAdditionalCapture && !capturedSoFar.isEmpty()) {
            validMoves.add(new Move(x, y, new ArrayList<>(capturedSoFar)));
        }
    }

    private void calculateKingMoves(Checker checker) {
        int x = checker.getCol();
        int y = checker.getRow();
        Color color = checker.getColor();

        if (!mustContinueCapture) {
            checkKingMoveInDirection(x, y, -1, -1, color); // Вверх-влево
            checkKingMoveInDirection(x, y, 1, -1, color);  // Вверх-вправо
            checkKingMoveInDirection(x, y, -1, 1, color);  // Вниз-влево
            checkKingMoveInDirection(x, y, 1, 1, color);   // Вниз-вправо
        }

        checkKingCaptures(x, y, color, new ArrayList<>(), copyBoard());
    }

    private void checkKingMoveInDirection(int startX, int startY, int dx, int dy, Color color) {
        for (int i = 1; i < BOARD_SIZE; i++) {
            int x = startX + dx * i;
            int y = startY + dy * i;

            if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE) break;

            if (board[y][x] == null) {
                validMoves.add(new Move(x, y, new ArrayList<>()));
            } else {
                break;
            }
        }
    }

    private void checkKingCaptures(int x, int y, Color color, List<Point> capturedSoFar, Checker[][] tempBoard) {
        int[][] directions = {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
        boolean foundCapture = false;

        for (int[] dir : directions) {
            Point enemyPos = findFirstEnemyInDirection(x, y, dir[0], dir[1], color, tempBoard);

            if (enemyPos != null) {
                int jumpX = enemyPos.x + dir[0];
                int jumpY = enemyPos.y + dir[1];

                while (jumpX >= 0 && jumpX < BOARD_SIZE && jumpY >= 0 && jumpY < BOARD_SIZE) {
                    if (tempBoard[jumpY][jumpX] == null && !isAlreadyCaptured(enemyPos.x, enemyPos.y, capturedSoFar)) {
                        List<Point> newCaptured = new ArrayList<>(capturedSoFar);
                        newCaptured.add(new Point(enemyPos.x, enemyPos.y));
                        foundCapture = true;

                        Checker[][] newTempBoard = copyBoard(tempBoard);
                        newTempBoard[enemyPos.y][enemyPos.x] = null;
                        newTempBoard[y][x] = null;
                        newTempBoard[jumpY][jumpX] = new Checker(color, jumpY, jumpX);

                        checkKingCaptures(jumpX, jumpY, color, newCaptured, newTempBoard);
                    } else if (tempBoard[jumpY][jumpX] != null) {
                        break;
                    }
                    jumpX += dir[0];
                    jumpY += dir[1];
                }
            }
        }

        if (!foundCapture && !capturedSoFar.isEmpty()) {
            validMoves.add(new Move(x, y, new ArrayList<>(capturedSoFar)));
        }
    }

    private Point findFirstEnemyInDirection(int startX, int startY, int dx, int dy, Color color, Checker[][] tempBoard) {
        for (int i = 1; i < BOARD_SIZE; i++) {
            int x = startX + dx * i;
            int y = startY + dy * i;

            if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE) return null;

            if (tempBoard[y][x] != null) {
                if (tempBoard[y][x].getColor() != color) {
                    return new Point(x, y);
                } else {
                    return null; // своя шашка блокирует путь
                }
            }
        }
        return null;
    }

    private boolean isValidCapture(int fromX, int fromY, int enemyX, int enemyY, int toX, int toY,
                                   Color color, List<Point> capturedSoFar) {
        return toX >= 0 && toX < BOARD_SIZE && toY >= 0 && toY < BOARD_SIZE &&
                board[enemyY][enemyX] != null && board[enemyY][enemyX].getColor() != color &&
                board[toY][toX] == null && !isAlreadyCaptured(enemyX, enemyY, capturedSoFar);
    }

    private boolean isAlreadyCaptured(int x, int y, List<Point> capturedSoFar) {
        for (Point p : capturedSoFar) {
            if (p.x == x && p.y == y) return true;
        }
        return false;
    }

    private boolean hasCaptureMoves() {
        return validMoves.stream().anyMatch(move -> !move.capturedCheckers.isEmpty());
    }

    private Checker[][] copyBoard() {
        return copyBoard(this.board);
    }

    private Checker[][] copyBoard(Checker[][] source) {
        Checker[][] copy = new Checker[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (source[i][j] != null) {
                    copy[i][j] = new Checker(source[i][j].getColor(), i, j);
                    copy[i][j].setKing(source[i][j].isKing());
                }
            }
        }
        return copy;
    }
}

