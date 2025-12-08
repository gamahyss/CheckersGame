package main.java;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CheckersGame().setVisible(true);
        });
    }
}

//ресет доски и чата после подключения к сетевой игре
//пофиксить отправку сигнала об отключении противника
//открыть порт во время демонстрации на паре
//игра зависает во время катки
//в какие-то моменты ходы и сообщения одного игрока перестают отправляться другому
//рассмотреть Spring