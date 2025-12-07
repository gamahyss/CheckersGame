package main.java;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class P2PNetworkManager {
    private ServerSocket serverSocket;
    private Socket peerSocket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private boolean isHost = false;
    private boolean connected = false;
    private String peerAddress = "";
    private int port = 12345;

    private CheckersGame game;
    private ExecutorService executor;

    public P2PNetworkManager(CheckersGame game) {
        this.game = game;
        this.executor = Executors.newFixedThreadPool(2);
    }

    public boolean startAsHost() {
        try {
            serverSocket = new ServerSocket(port);
            isHost = true;

            JOptionPane.showMessageDialog(game,
                    "Ожидание подключения противника...\nВаш IP: " + getLocalIP() +
                            "\nПорт: " + port);

            // Запускаем в отдельном потоке ожидание подключения
            executor.execute(() -> {
                try {
                    peerSocket = serverSocket.accept();
                    outputStream = new ObjectOutputStream(peerSocket.getOutputStream());
                    inputStream = new ObjectInputStream(peerSocket.getInputStream());
                    connected = true;

                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(game, "Противник подключился!");
                        game.setNetworkConnected(true, false); // Хост играет белыми
                    });

                    startListening();

                } catch (IOException e) {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(game, "Ошибка подключения: " + e.getMessage()));
                }
            });

            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(game, "Не удалось создать сервер: " + e.getMessage());
            return false;
        }
    }

    public boolean connectAsClient(String hostAddress) {
        try {
            peerSocket = new Socket(hostAddress, port);
            outputStream = new ObjectOutputStream(peerSocket.getOutputStream());
            inputStream = new ObjectInputStream(peerSocket.getInputStream());
            isHost = false;
            connected = true;
            peerAddress = hostAddress;

            // Отправляем приветственное сообщение
            sendMessage(new NetworkMessage(NetworkMessage.MessageType.CONNECT,
                    "Подключение установлено"));

            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(game, "Успешно подключено к " + hostAddress);
                game.setNetworkConnected(true, true); // Клиент играет черными
            });

            startListening();
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(game, "Не удалось подключиться: " + e.getMessage());
            return false;
        }
    }

    private void startListening() {
        executor.execute(() -> {
            try {
                while (connected && !peerSocket.isClosed()) {
                    NetworkMessage message = (NetworkMessage) inputStream.readObject();
                    processMessage(message);
                }
            } catch (EOFException e) {
                System.out.println("Соединение закрыто");
            } catch (IOException | ClassNotFoundException e) {
                if (connected) {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(game, "Соединение разорвано"));
                    disconnect();
                }
            }
        });
    }

    // Обработка входящих сообщений
    private void processMessage(NetworkMessage message) {
        SwingUtilities.invokeLater(() -> {
            switch (message.getType()) {
                case MOVE:
                    MoveData moveData = (MoveData) message.getData();
                    game.applyNetworkMove(moveData);
                    break;

                case GAME_STATE:
                    // Можно использовать для синхронизации состояния
                    break;

                case CHAT:
                    String chatMsg = (String) message.getData();
                    game.receiveChatMessage(chatMsg);
                    break;

                case DISCONNECT:
                    JOptionPane.showMessageDialog(game, "Противник отключился");
                    disconnect();
                    break;

                case ERROR:
                    JOptionPane.showMessageDialog(game, "Ошибка: " + message.getData());
                    break;
            }
        });
    }

    // Отправка сообщения
    public void sendMessage(NetworkMessage message) {
        if (!connected || outputStream == null) return;

        try {
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (IOException e) {
            System.err.println("Ошибка отправки сообщения: " + e.getMessage());
        }
    }

    // Отправка хода
    public void sendMove(MoveData moveData) {
        sendMessage(new NetworkMessage(NetworkMessage.MessageType.MOVE, moveData));
    }

    // Отправка сообщения чата
    public void sendChatMessage(String message) {
        sendMessage(new NetworkMessage(NetworkMessage.MessageType.CHAT, message));
    }

    // Отключение
    public void disconnect() {
        connected = false;

        if (outputStream != null) {
            sendMessage(new NetworkMessage(NetworkMessage.MessageType.DISCONNECT, "Отключение"));
        }

        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (peerSocket != null && !peerSocket.isClosed()) peerSocket.close();
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        executor.shutdown();
        SwingUtilities.invokeLater(() -> game.setNetworkConnected(false, false));
    }

    private String getLocalIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }

    public boolean isConnected() { return connected; }
    public boolean isHost() { return isHost; }
    public String getPeerAddress() { return peerAddress; }
}