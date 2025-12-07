package main.java;

import main.java.Checker;
import main.java.Move;
import main.java.P2PNetworkManager;
import main.java.MoveData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.awt.Point;

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
    private JPanel bottomPanel;
    private JLabel statusLabel;
    private JButton restartButton;
    private JButton networkButton;
    private JTextArea chatArea;
    private JTextField chatInput;
    private boolean hasMadeMove = false;
    private boolean fullScreenMode = false;
    private CheckersPanel gamePanel;

    private P2PNetworkManager networkManager;
    private boolean networkGame = false;
    private boolean myTurn = true;
    private boolean isWhitePlayer = true;
    private JPanel networkPanel;

    public CheckersGame() {
        setTitle("Шашки");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        networkManager = new P2PNetworkManager(this);

        networkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        networkButton = new JButton("Сетевая игра");
        networkButton.addActionListener(e -> showNetworkDialog());

        JButton disconnectButton = new JButton("Отключиться");
        disconnectButton.addActionListener(e -> disconnectNetwork());

        networkPanel.add(networkButton);
        networkPanel.add(disconnectButton);
        add(networkPanel, BorderLayout.NORTH);

        gamePanel = new CheckersPanel();
        add(gamePanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(250, 0));

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setPreferredSize(new Dimension(250, 600));

        JPanel chatInputPanel = new JPanel(new BorderLayout());
        chatInput = new JTextField();
        chatInput.addActionListener(e -> sendChatMessage());
        JButton sendButton = new JButton("Отправить");
        sendButton.addActionListener(e -> sendChatMessage());

        chatInputPanel.add(chatInput, BorderLayout.CENTER);
        chatInputPanel.add(sendButton, BorderLayout.EAST);

        rightPanel.add(new JLabel("Чат:", SwingConstants.CENTER), BorderLayout.NORTH);
        rightPanel.add(chatScroll, BorderLayout.CENTER);
        rightPanel.add(chatInputPanel, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.EAST);

        bottomPanel = new JPanel(new BorderLayout());

        statusLabel = new JLabel("Ход белых", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        bottomPanel.add(statusLabel, BorderLayout.CENTER);

        restartButton = new JButton("Начать заново");
        restartButton.setFont(new Font("Arial", Font.PLAIN, 12));
        restartButton.setPreferredSize(new Dimension(120, 30));
        restartButton.addActionListener(e -> resetGame());
        bottomPanel.add(restartButton, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Игра");
        JMenu networkMenu = new JMenu("Сеть");

        JMenuItem newGameItem = new JMenuItem("Новая игра");
        JMenuItem fullScreenItem = new JMenuItem("Полноэкранный режим");
        JMenuItem exitItem = new JMenuItem("Выход");

        JMenuItem hostGameItem = new JMenuItem("Создать игру");
        JMenuItem joinGameItem = new JMenuItem("Присоединиться");
        JMenuItem disconnectMenuItem = new JMenuItem("Отключиться");

        newGameItem.addActionListener(e -> resetGame());
        fullScreenItem.addActionListener(e -> toggleFullScreen());
        exitItem.addActionListener(e -> System.exit(0));

        hostGameItem.addActionListener(e -> createNetworkGame());
        joinGameItem.addActionListener(e -> joinNetworkGame());
        disconnectMenuItem.addActionListener(e -> disconnectNetwork());

        gameMenu.add(newGameItem);
        gameMenu.add(fullScreenItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);

        networkMenu.add(hostGameItem);
        networkMenu.add(joinGameItem);
        networkMenu.addSeparator();
        networkMenu.add(disconnectMenuItem);

        menuBar.add(gameMenu);
        menuBar.add(networkMenu);
        setJMenuBar(menuBar);

        initializeBoard();
        updateStatus();

        setPreferredSize(new Dimension(1100, 850));
        pack();
        setLocationRelativeTo(null);
    }

    private void showNetworkDialog() {
        String[] options = {"Создать игру", "Присоединиться", "Отмена"};
        int choice = JOptionPane.showOptionDialog(this,
                "Выберите тип подключения:",
                "Сетевая игра",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0) {
            createNetworkGame();
        } else if (choice == 1) {
            joinNetworkGame();
        }
    }

    private void createNetworkGame() {
        if (networkManager.isConnected()) {
            JOptionPane.showMessageDialog(this, "Уже подключены к игре");
            return;
        }

        if (networkManager.startAsHost()) {
            addChatMessage("Система: Игра создана. Ожидание противника...");
        }
    }

    private void joinNetworkGame() {
        if (networkManager.isConnected()) {
            JOptionPane.showMessageDialog(this, "Уже подключены к игре");
            return;
        }

        String ipAddress = JOptionPane.showInputDialog(this,
                "Введите IP адрес хоста:", "127.0.0.1");

        if (ipAddress != null && !ipAddress.trim().isEmpty()) {
            if (networkManager.connectAsClient(ipAddress.trim())) {
                addChatMessage("Система: Подключение к " + ipAddress);
            }
        }
    }

    private void disconnectNetwork() {
        if (networkManager.isConnected()) {
            networkManager.disconnect();
            networkGame = false;
            addChatMessage("Система: Отключено от сетевой игры");
            updateStatus();
        }
    }

    public void setNetworkConnected(boolean connected, boolean isWhite) {
        this.networkGame = connected;
        this.isWhitePlayer = isWhite;
        this.myTurn = isWhite;

        if (connected) {
            addChatMessage("Система: Сетевая игра начата!");
            addChatMessage("Система: Вы играете " + (isWhite ? "белыми" : "черными"));

            if (!isWhite) {
                statusLabel.setText("Ожидаем ход белых...");
            }
        }

        updateStatus();
    }

    public void applyNetworkMove(MoveData moveData) {
        if (!networkGame) return;

        Checker checker = board[moveData.getFromRow()][moveData.getFromCol()];
        if (checker == null) return;

        makeMove(checker, moveData.getToCol(), moveData.getToRow(), moveData.getCaptured());

        myTurn = true;
        updateStatus();
    }

    public void makeMove(Checker checker, int newCol, int newRow, List<Point> capturedCheckers) {
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

        if (!networkGame) {
            isWhiteTurn = !isWhiteTurn;
        }

        hasMadeMove = false;
        updateStatus();
        repaint();
    }

    private void sendNetworkMove(Checker checker, int toCol, int toRow, List<Point> captured) {
        if (!networkGame || !myTurn) return;

        MoveData moveData = new MoveData(
                checker.getRow(),
                checker.getCol(),
                toRow,
                toCol,
                captured
        );

        networkManager.sendMove(moveData);
        myTurn = false;
    }

    private void sendChatMessage() {
        String message = chatInput.getText().trim();
        if (!message.isEmpty() && networkManager.isConnected()) {
            networkManager.sendChatMessage(message);
            addChatMessage("Вы: " + message);
            chatInput.setText("");
        }
    }

    public void receiveChatMessage(String message) {
        addChatMessage("Противник: " + message);
    }

    private void addChatMessage(String message) {
        chatArea.append(message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void initializeBoard() {
        board = new Checker[BOARD_SIZE][BOARD_SIZE];

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
        String status;

        if (networkGame) {
            status = "Сетевая игра | ";
            status += "Вы: " + (isWhitePlayer ? "Белые" : "Черные") + " | ";
            status += myTurn ? "Ваш ход" : "Ход противника";
        } else {
            status = isWhiteTurn ? "Ход белых" : "Ход черных";
        }

        if (mustContinueCapture) {
            status += " (продолжите взятие)";
        }

        status += " | Белые: " + whiteCheckersCount + " | Черные: " + blackCheckersCount;
        statusLabel.setText(status);

        if (whiteCheckersCount == 0) {
            JOptionPane.showMessageDialog(this, "Черные выиграли!");
            if (networkGame) {
                addChatMessage("Система: Черные выиграли!");
            }
        } else if (blackCheckersCount == 0) {
            JOptionPane.showMessageDialog(this, "Белые выиграли!");
            if (networkGame) {
                addChatMessage("Система: Белые выиграли!");
            }
        }
    }

    private void resetGame() {
        int response = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите начать новую игру?",
                "Новая игра",
                JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.YES_OPTION) {
            board = new Checker[BOARD_SIZE][BOARD_SIZE];
            whiteCheckersCount = blackCheckersCount = 12;
            isWhiteTurn = true;
            mustContinueCapture = false;
            selectedChecker = null;
            validMoves.clear();
            checkerInCaptureSequence = null;
            hasMadeMove = false;

            if (networkGame) {
                myTurn = isWhitePlayer; // Сброс очереди хода в сетевой игре
            }

            // Инициализируем новую доску
            initializeBoard();

            // Обновляем интерфейс
            gamePanel.repaint();
            updateStatus();

            JOptionPane.showMessageDialog(this, "Новая игра начата!");
            if (networkGame) {
                addChatMessage("Система: Игра перезапущена!");
            }
        }
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
                if (networkGame && !myTurn) {
                    JOptionPane.showMessageDialog(CheckersGame.this,
                            "Сейчас ход противника!");
                    return;
                }

                Point boardPos = screenToBoard(e.getX(), e.getY());
                if (boardPos == null) return;

                int col = boardPos.x;
                int row = boardPos.y;

                Color currentColor = networkGame ?
                        (isWhitePlayer ? Color.WHITE : Color.BLACK) :
                        (isWhiteTurn ? Color.WHITE : Color.BLACK);

                if (board[row][col] != null && board[row][col].getColor() == currentColor) {
                    if (mustContinueCapture && selectedChecker != null && selectedChecker != board[row][col]) {
                        return; // Нельзя выбрать другую шашку во время взятия
                    }

                    selectedChecker = board[row][col];
                    calculateValidMoves(selectedChecker);

                    if (hasMandatoryCapture() && !hasCaptureMoves()) {
                        selectedChecker = null;
                        validMoves.clear();
                    }

                    repaint();
                    return;
                }

                if (selectedChecker != null) {
                    for (Move move : validMoves) {
                        if (move.toCol == col && move.toRow == row) {
                            if (networkGame) {
                                // В сетевой игре отправляем ход
                                sendNetworkMove(selectedChecker, col, row, move.capturedCheckers);
                            }
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

                if (!networkGame) {
                    isWhiteTurn = !isWhiteTurn;
                }

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

        if (hasCaptureMoves()) {
            validMoves.removeIf(move -> move.capturedCheckers.isEmpty());
        }
    }

    private void calculateNormalMoves(Checker checker) {
        int x = checker.getCol();
        int y = checker.getRow();
        Color color = checker.getColor();
        int direction = (color == Color.WHITE) ? -1 : 1;

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