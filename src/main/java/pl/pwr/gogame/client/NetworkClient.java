package pl.pwr.gogame.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import pl.pwr.gogame.client.view.BoardCanvas;
import pl.pwr.gogame.model.StoneColor;

public class NetworkClient {
    

    private PrintWriter out;
    private final BoardCanvas boardCanvas;

    //javafx zapewnia nam klasę TextArea służącą
    //do przechowywania wielu linijek tekstu.
    //Tutaj służy do przechowywania logów
    private final TextArea logArea;
    

    public NetworkClient(String host, int port, BoardCanvas boardCanvas, TextArea logArea) {
        this.boardCanvas = boardCanvas;
        this.logArea = logArea;

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
            if (msg.startsWith("MOVE")) {
                String[] parts = msg.split(" ");
                int col = Integer.parseInt(parts[1]);
                int row = Integer.parseInt(parts[2]);
                StoneColor color = StoneColor.valueOf(parts[3]);

                Platform.runLater(() ->
                    boardCanvas.drawStone(col, row, color)
                );

            } else if (msg.startsWith("CAPTURE")) {
                // usuwanie kamienia na danym miejscu

                String[] parts = msg.split(" ");
                int col = Integer.parseInt(parts[1]);
                int row = Integer.parseInt(parts[2]);

                 Platform.runLater(() ->
                    boardCanvas.removeStone(col, row)
                );

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
            logArea.appendText(message + "\n")
        );
}
}
