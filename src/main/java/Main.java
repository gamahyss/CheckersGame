package main.java;

import main.java.CheckersGame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CheckersGame().setVisible(true);
        });
    }
}