//НЕ ЗАБУДЬ ОТКРЫТЬ ПОРТ ВО ВРЕМЯ ДЕМОНСТРАЦИИ НА ПАРЕ

package main.java;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import javax.swing.*;

@SpringBootApplication
public class CheckersApplication {
    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");
        new Thread(() -> {
            SpringApplication.run(CheckersApplication.class, args);
            System.out.println("Spring Boot сервер запущен на порту 8080");
        }).start();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            CheckersGame game = new CheckersGame();
            game.setVisible(true);
            System.out.println("Swing GUI запущена");
        });
    }
}