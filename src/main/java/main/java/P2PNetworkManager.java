package main.java;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class P2PNetworkManager {
    private ServerSocket serverSocket;
    private Socket peerSocket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private boolean isHost = false;
    private AtomicBoolean connected = new AtomicBoolean(false);
    private String peerAddress = "";
    private int port = 8002;

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
            connected.set(true);

            SwingUtilities.invokeLater(() -> {
                String ip = getLocalIP();
                JOptionPane.showMessageDialog(game,
                        "Ожидание подключения противника...\n" +
                                "Ваш IP: " + ip + "\nПорт: " + port);
                game.addChatMessage("Система: Создана игра. IP: " + ip + " Порт: " + port);
            });

            executor.execute(() -> {
                try {
                    peerSocket = serverSocket.accept();
                    outputStream = new ObjectOutputStream(peerSocket.getOutputStream());
                    inputStream = new ObjectInputStream(peerSocket.getInputStream());

                    sendMessage(new NetworkMessage(NetworkMessage.MessageType.CONNECT,
                            "HOST_CONNECTED"));

                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(game, "Противник подключился!");
                        game.setNetworkConnected(true, false); // Хост играет белыми
                    });

                    startListening();

                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> {
                        if (connected.get()) {
                            JOptionPane.showMessageDialog(game, "Ошибка подключения: " + e.getMessage());
                            disconnect();
                        }
                    });
                }
            });

            return true;
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(game, "Не удалось создать сервер: " + e.getMessage());
                if (e instanceof BindException) {
                    JOptionPane.showMessageDialog(game,
                            "Порт " + port + " уже занят. Попробуйте другой порт.");
                }
            });
            return false;
        }
    }

    public boolean connectAsClient(String hostAddress) {
        try {
            peerSocket = new Socket();
            peerSocket.connect(new InetSocketAddress(hostAddress, port), 5000);

            outputStream = new ObjectOutputStream(peerSocket.getOutputStream());
            inputStream = new ObjectInputStream(peerSocket.getInputStream());
            isHost = false;
            connected.set(true);
            peerAddress = hostAddress;

            sendMessage(new NetworkMessage(NetworkMessage.MessageType.CONNECT,
                    "CLIENT_CONNECTED"));

            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(game, "Успешно подключено к " + hostAddress);
                game.setNetworkConnected(true, true); // Клиент играет черными
            });

            startListening();
            return true;
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(game, "Не удалось подключиться: " + e.getMessage());
                if (e instanceof ConnectException) {
                    JOptionPane.showMessageDialog(game,
                            "Не удалось подключиться к " + hostAddress + ":" + port +
                                    "\nУбедитесь, что хост запущен и порт открыт.");
                }
            });
            return false;
        }
    }

    private void startListening() {
        executor.execute(() -> {
            try {
                while (connected.get() && peerSocket != null && !peerSocket.isClosed()) {
                    NetworkMessage message = (NetworkMessage) inputStream.readObject();
                    processMessage(message);
                }
            } catch (EOFException e) {
                System.out.println("Соединение закрыто корректно");
            } catch (IOException | ClassNotFoundException e) {
                if (connected.get()) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(game, "Соединение разорвано: " + e.getMessage());
                        disconnect();
                    });
                }
            }
        });
    }

    private void processMessage(NetworkMessage message) {
        SwingUtilities.invokeLater(() -> {
            try {
                switch (message.getType()) {
                    case CONNECT:
                        break;

                    case MOVE:
                        MoveData moveData = (MoveData) message.getData();
                        game.applyNetworkMove(moveData);
                        break;

                    case GAME_STATE:
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
            } catch (Exception e) {
                System.err.println("Ошибка обработки сообщения: " + e.getMessage());
            }
        });
    }

    public void sendMessage(NetworkMessage message) {
        if (!connected.get() || outputStream == null) {
            System.err.println("Не удалось отправить сообщение: соединение не установлено");
            return;
        }

        synchronized (this) {
            try {
                outputStream.writeObject(message);
                outputStream.flush();
            } catch (IOException e) {
                System.err.println("Ошибка отправки сообщения: " + e.getMessage());
                if (connected.get()) {
                    disconnect();
                }
            }
        }
    }

    public void sendMove(MoveData moveData) {
        sendMessage(new NetworkMessage(NetworkMessage.MessageType.MOVE, moveData));
    }

    public void sendChatMessage(String message) {
        sendMessage(new NetworkMessage(NetworkMessage.MessageType.CHAT, message));
    }

    public void disconnect() {
        if (!connected.getAndSet(false)) {
            return;
        }

        if (outputStream != null) {
            try {
                sendMessage(new NetworkMessage(NetworkMessage.MessageType.DISCONNECT, "Отключение"));
            } catch (Exception e) {

            }
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

        SwingUtilities.invokeLater(() -> {
            if (game != null) {
                game.setNetworkConnected(false, false);
            }
        });
    }

    private String getLocalIP() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("google.com", 80));
            String ip = socket.getLocalAddress().getHostAddress();
            socket.close();
            return ip;
        } catch (Exception e) {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException ex) {
                return "127.0.0.1";
            }
        }
    }

    public boolean isConnected() { return connected.get(); }
    public boolean isHost() { return isHost; }
    public String getPeerAddress() { return peerAddress; }
}