// sluzy do obslugi klienta w serwerze gry Go
package pl.pwr.gogame.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

import pl.pwr.gogame.model.Board;
import pl.pwr.gogame.model.GameEngine;
import pl.pwr.gogame.model.GamePlayer;
import pl.pwr.gogame.model.Move;
import pl.pwr.gogame.model.MoveResult;
import pl.pwr.gogame.model.Position;
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
            command = command.trim();

            // Obsługa rezygnacji
            if (command.equalsIgnoreCase("resign")) {
                engine.resign(player);
                MoveResult resignResult = engine.resign(player);
                if (opponent != null) {
                    opponent.sendResign(resignResult);
                }
             closeConnection();
                if (opponent != null) {
                    opponent.closeConnection();
                }
                return;
            }
             if (command.equalsIgnoreCase("pass") || command.equals("pas")) {
            MoveResult result = engine.pass(player);
            
            if (result.isOk()) {
                sendPass(player);
                if (opponent != null) opponent.sendPass(player);
            }
            if (opponent != null) {
                opponent.sendPass(player);
            }

            
            if (result.isEnd()) {
                // Oboje gracze spasowali - KONIEC GRY I LICZENIE PUNKTÓW
                ScoreResult scores = engine.calculateScores();
                String scoreMessage = ResponseFormatter.formatScores(scores);
                
                sendText(scoreMessage);
                if (opponent != null) {
                    opponent.sendText(scoreMessage);
                }
            } else {
                notifyPlayers(null, null);
            }
            return;
        }

            // Parsowanie i wykonanie ruchu
            Move move = CommandParser.parseMove(command, this.player);
            MoveResult result = engine.applyMove(move);
            //Uwaga- ruchy które mogą być invalid
            //w pewnych przypadkach, np. PASS i MOVE,
            //wysyłane są tylko jeśli są poprawne.
            //W przeciwnym wypadku np. rysowalibyśmy
            //w GUI kamień nawet, gdy ruch był w złej turze
            if (result.isOk()) {
                //Ruch wysyłamy do GUI funkcją sendMove 
                sendMove(move, result);
                if (opponent != null) {
                    opponent.sendMove(move, result);
                }
            }
            //Funkcją sendText wysyłamy ruch do terminala
            sendText(ResponseFormatter.formatMoveResult(result));
            
            if (result.isOk()) {
                notifyPlayers(null, null);
            }
        } catch (IllegalArgumentException e) {
            sendText("BŁĄD WEJŚCIA: " + e.getMessage());
        }
    }


  

    private void notifyPlayers(Move move, MoveResult result) {
           
        String statusMsg = ResponseFormatter.formatStatus(
                engine.getCurrentPlayer(),
                engine.getCurrentColor()
        );
        sendText(statusMsg);

        // Wysyłanie do przeciwnika
        if (opponent != null) {
            opponent.sendText(statusMsg);
        }
    }


    //W NetworkClient zczytujemy pierwszy wyraz z funkcji send,
    //co umożliwi poprawną aktualizację na planszy w GUI
    //w zależności od typu zdarzenia
     private void sendMove(Move move, MoveResult result) {
        //jak nie było stawiania kamienia, np. pas lub resign, kończymy funkcję
        if (result == null) return; 
        
        //wysyłanie ruchu
        send("MOVE " + move.getPosition().col() + " " +
                     move.getPosition().row() + " " +
                     move.getPlayer().getColor());

        //wysyłanie listy przejętych kamieni
        List<Position> captured = result.getCapturedPositions();
        for (Position pos : captured) {
            send("CAPTURE " + pos.col() + " " + pos.row());
        }
    }

    //wysyłanie PASS do GUI lub terminala
    private void sendPass(GamePlayer player) {
        send("PASS " + player.getColor());
    }

    //wysyłanie RESIGN do GUI lub terminala
    private void sendResign(MoveResult result) {
        send("RESIGN " + result.getLoser().getColor() + " " + result.getWinner().getColor());
    }

    /** Wysyłanie tekstu do GUI lub terminala */
    private void sendText(String message) {
        send("TEXT " + message);
    }
    
    public void closeConnection() {
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Błąd podczas zamykania połączenia: " + e.getMessage());
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