package pl.pwr.gogame.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import pl.pwr.gogame.client.view.BoardCanvas;
import pl.pwr.gogame.client.view.GameView;
import pl.pwr.gogame.model.StoneColor;

public class NetworkClient {
    

    private PrintWriter out;


    //javafx zapewnia nam klasę TextArea służącą
    //do przechowywania wielu linijek tekstu.
    //Tutaj służy do przechowywania logów

    private final GameView view;
    private final GameController controller;
    

    public NetworkClient(String host, int port, GameView view,
                     GameController controller) {
        this.view = view;
        this.controller = controller;

        try {
            Socket socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            new Thread(() -> listen(socket)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listen(Socket socket) {
    try (Scanner in = new Scanner(socket.getInputStream())) {


        while (in.hasNextLine()) {
            String msg = in.nextLine();
            System.out.println("SERVER: " + msg);

            // W zależności od typu zdarzenia wywoływane są
            // dane metody
            // Np. dla MOVE dzielimy wiadomość np "1 2" na
            // koordynaty i rysujemy kamień na tym miejscu

            //dopóki drugi gracz się nie połączy, klikanie pierwszego w GUI nic nie robi
            if (msg.equals("GAME_START")) {
                log("Gra się rozpoczęła.");
            }

            if (msg.equals("YOUR_TURN")) {
                Platform.runLater(() -> {
                    view.getBoardCanvas().setDisable(false);
                    log("Twój ruch.");
                });
            }

            if (msg.equals("OPPONENT_TURN")) {
                Platform.runLater(() -> {
                    view.getBoardCanvas().setDisable(true);
                    log("Ruch przeciwnika.");
                });
            }

            if (msg.startsWith("CONFIG BOARD_SIZE")) {
                int size = Integer.parseInt(msg.split(" ")[2]);

                Platform.runLater(() -> {
                    view.createBoard(size);
                    controller.registerBoardHandlers();
                });
                continue;
            }

            if (msg.startsWith("MOVE")) {
                String[] parts = msg.split(" ");
                int col = Integer.parseInt(parts[1]);
                int row = Integer.parseInt(parts[2]);
                StoneColor color = StoneColor.valueOf(parts[3]);

                Platform.runLater(() -> {
                    BoardCanvas board = view.getBoardCanvas();
                    if (board != null) {
                        board.drawStone(col, row, color);
                    }
                });

            } else if (msg.startsWith("CAPTURE")) {
                // usuwanie kamienia na danym miejscu

                String[] parts = msg.split(" ");
                int col = Integer.parseInt(parts[1]);
                int row = Integer.parseInt(parts[2]);

                 Platform.runLater(() -> {
                    BoardCanvas board = view.getBoardCanvas();
                    if (board != null) {
                        board.removeStone(col, row);
                    }
                });

            } else if (msg.startsWith("PASS")) {
                //wpisywanie pasu do logów
                log("Przeciwnik zpasował.");
            } else if (msg.startsWith("RESIGN")) {
                // update board/log
                 String[] parts = msg.split(" ");
                log("Koniec gry. Wygrał: " + parts[2]);
            } else if (msg.startsWith("TEXT")) {
                //Wiadomości są schematu: "TEXT: info",
                //więc do logów wpisujemy samo info
                //i usuwamy keyword "TEXT" z początku wiadomości
                 log(msg.substring(5));
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}

      public void send(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }
        //funkcja do dodania linijki z opisem zdarzenia do 
        //naszych logów za pomocą dołączonej do klasy TextArea
        //funkcji appendText
        private void log(String message) {
        Platform.runLater(() ->
            view.getLogArea().appendText(message + "\n")
        );
}
}
