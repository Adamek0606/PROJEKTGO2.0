// glowny plik uruchomieniowy serwera gry Go
// Composite, poniewaz sklada sie z wielu komponentow
package pl.pwr.gogame.service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import pl.pwr.gogame.model.Board; 
import pl.pwr.gogame.model.BoardFactory;
import pl.pwr.gogame.model.GameEngine;
import pl.pwr.gogame.model.GamePlayer;
import pl.pwr.gogame.model.StoneColor;
import pl.pwr.gogame.server.ClientHandler;

public class Main {
    public static void main(String[] args) throws IOException {
       
            ServerSocket serverSocket = new ServerSocket(58901);
            System.out.println("Serwer Go działa...");

            System.out.println("Witaj w Go!");
            Scanner scanner = new Scanner(System.in);

            int boardSize = 0;
            Board board = null;
            // dopóki nie utworzymy poprawnej planszy, pytamy użytkownika o rozmiar
            while (board == null) {


                System.out.println("Wybierz rozmiar planszy: 9, 13, 19");
                String sizeInput = scanner.nextLine().trim();
                try {
                    boardSize = Integer.parseInt(sizeInput);
                    try {
                        board = BoardFactory.createBoard(boardSize);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Nieprawidłowy rozmiar planszy. Dozwolone wartości: 9, 13, 19.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("To nie jest liczba. Podaj 9, 13 lub 19.");
                }
            }
        GameEngine gameEngine = new GameEngine(board);

        // Stworzenie dwóch lokalnych graczy (dla trybu konsolowego)
        GamePlayer blackPlayer = new GamePlayer("BlackPlayer", StoneColor.BLACK);
        GamePlayer whitePlayer = new GamePlayer("WhitePlayer", StoneColor.WHITE);
        // Zarejestruj graczy w silniku gry
        gameEngine.setPlayers(blackPlayer, whitePlayer);

    // Powiadomienie w konsoli: serwer teraz będzie oczekiwał na połączenia graczy
    System.out.println("OCZEKIWANIE: Oczekiwanie na dołączenie graczy... (oczekuję na 2 połączenia)");

                // Akceptuj pierwszego klienta i uruchom jego obsługę natychmiast
                Socket socket1 = serverSocket.accept();
                System.out.println("Gracz BLACK połączył się");
                ClientHandler black = new ClientHandler(socket1, gameEngine, blackPlayer, board);
                new Thread(black).start();

                // Akceptuj drugiego klienta i uruchom jego obsługę
                Socket socket2 = serverSocket.accept();
                System.out.println("Gracz WHITE połączył się");
                ClientHandler white = new ClientHandler(socket2, gameEngine, whitePlayer, board);
                new Thread(white).start();

                // Po uruchomieniu obu handlerów — ustaw przeciwników i powiadomienia
                black.setOpponent(white);
                white.setOpponent(black);


           

    }
} 