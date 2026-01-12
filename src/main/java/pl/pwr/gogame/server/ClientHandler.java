// sluzy do obslugi klienta w serwerze gry Go
package pl.pwr.gogame.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import pl.pwr.gogame.model.Board;
import pl.pwr.gogame.model.GameEngine;
import pl.pwr.gogame.model.GamePlayer;
import pl.pwr.gogame.model.Move;
import pl.pwr.gogame.model.MoveResult;
import pl.pwr.gogame.model.ScoreResult;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final GameEngine engine;
    private final GamePlayer player;
    private final Board board;
    private ClientHandler opponent;
    private PrintWriter out;

    public ClientHandler(Socket socket, GameEngine engine, GamePlayer player, Board board) {
        this.socket = socket;
        this.engine = engine;
        this.player = player;
        this.board = board;
    }

    @Override
    public void run() {
        try (Scanner in = new Scanner(socket.getInputStream());
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            this.out = writer;

            // Używamy nowej klasy do sformatowania powitania
            send(ResponseFormatter.formatWelcome(board));

            // Jeśli przeciwnik jeszcze nie dołączył — poinformuj gracza, że czekamy
            if (opponent == null) {
                send("OCZEKIWANIE: Oczekiwanie na dołączenie przeciwnika...");
            }

            while (in.hasNextLine()) {
                String command = in.nextLine();
                handleCommand(command);
            }

        } catch (IOException e) {
            System.err.println("Gracz rozłączony: " + player.getName());
        } finally {
            handleDisconnect();
        }
    }

    private void handleCommand(String command) {
        try {
            command = command.trim().toLowerCase();

            // Obsługa rezygnacji
            if (command.equals("resign")) {
                engine.resign(player);
                send("Koniec gry. Poddałeś się.");
                if (opponent != null) {
                    opponent.send("Koniec gry. Przeciwnik się poddał. Wygrałeś.");
                }
                return; // Zakończ obsługę komendy
            }
            
             if (command.equals("pass") || command.equals("pas")) {
            MoveResult result = engine.pass(player);
            send("Pasujesz.");
            if (opponent != null) {
                opponent.send("Przeciwnik spasował.");
            }
            
            if (result.isEnd()) {
                // Oboje gracze spasowali - KONIEC GRY I LICZENIE PUNKTÓW
                ScoreResult scores = engine.calculateScores();
                String scoreMessage = ResponseFormatter.formatScores(scores);
                
                send(scoreMessage);
                if (opponent != null) {
                    opponent.send(scoreMessage);
                }
            } else {
                notifyPlayers();
            }
            return;
        }

            // Parsowanie i wykonanie ruchu
            Move move = CommandParser.parseMove(command, this.player);
            MoveResult result = engine.applyMove(move);
            send(ResponseFormatter.formatMoveResult(result));
            
            if (result.isOk()) {
                notifyPlayers();
            }
        } catch (IllegalArgumentException e) {
            send("BŁĄD WEJŚCIA: " + e.getMessage());
        }
    }

    private void notifyPlayers() {
        // Przygotowanie danych do wyświetlenia
        String boardView = board.toString();
        String statusMsg = ResponseFormatter.formatStatus(
                engine.getCurrentPlayer(),
                engine.getCurrentColor()
        );

        // Wyślij do siebie
        send(boardView);
        send(statusMsg);

        // Wyślij do przeciwnika
        if (opponent != null) {
            opponent.send("Przeciwnik wykonał ruch.");
            opponent.send(boardView);
            opponent.send(statusMsg);
        }
    }

    private void handleDisconnect() {
        try {
            socket.close();
        } catch (IOException ignored) {}
        
        if (opponent != null) {
            opponent.send("Przeciwnik rozłączył się. Koniec gry.");
        }
    }


    public void setOpponent(ClientHandler opponent) {
        this.opponent = opponent;
        // Jeśli ustawiono przeciwnika, poinformuj obie strony, że gra może się rozpocząć
        if (opponent != null) {
            send("INFO: Przeciwnik dołączył. Gra się rozpoczyna.");
        }
    }

    public void send(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}