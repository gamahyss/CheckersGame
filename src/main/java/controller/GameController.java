package controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.security.Principal;

@Controller
public class GameController {

    private final SimpMessagingTemplate messagingTemplate;

    public GameController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/game.move")
    public void handleMove(main.java.MoveData moveData, Principal principal) {
        messagingTemplate.convertAndSend("/topic/game.moves", moveData);
    }

    @MessageMapping("/game.chat")
    public void handleChat(String message, Principal principal) {
        ChatMessage chatMsg = new ChatMessage(principal.getName(), message);
        messagingTemplate.convertAndSend("/topic/game.chat", chatMsg);
    }

    @MessageMapping("/game.connection")
    public void handleConnection(ConnectionMessage connection, Principal principal) {
        messagingTemplate.convertAndSend("/topic/game.connections", connection);
    }
}

