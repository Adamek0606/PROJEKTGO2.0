package pl.pwr.gogame.client;

import pl.pwr.gogame.client.view.BoardCanvas;
import pl.pwr.gogame.client.view.GameView;

/**
 * Klasa {@code GameController} odpowiada za logikę sterującą grą
 * po stronie klienta. Łączy warstwę widoku ({@link GameView})
 * z komunikacją sieciową realizowaną przez {@link NetworkClient}
 * oraz obsługuje interakcje użytkownika.
 */
public class GameController {

    /**
     * Widok gry, z którym powiązany jest kontroler.
     */
    private final GameView view;

    /**
     * Klient sieciowy odpowiedzialny za komunikację z serwerem gry.
     */
    private final NetworkClient client;

    /**
     * Tworzy nowy kontroler gry.
     * Inicjalizuje klienta sieciowego oraz rejestruje obsługę
     * przycisków sterujących (Pass, Resign).
     *
     * @param view widok gry
     */
    public GameController(GameView view) {
        this.view = view;
        this.client = new NetworkClient(
                "localhost",
                58901,
                view, this
        );

        // pass and resign buttons
        view.getPassButton().setOnAction(e -> {
            client.send("pass");
        });

        view.getResignButton().setOnAction(e -> {
            client.send("resign");
        });
    }

    /**
     * Rejestruje obsługę zdarzeń myszy na planszy gry.
     * Metoda mapuje współrzędne kliknięcia myszy na odpowiednie
     * pole planszy i wysyła wybrany ruch do serwera.
     */
    public void registerBoardHandlers() {
        BoardCanvas board = view.getBoardCanvas();
        board.setOnMouseClicked(e -> {
            int size = board.getSize();
            double cellWidth = board.getWidth() / size;
            double cellHeight = board.getHeight() / size;
            double offsetX = cellWidth / 2.0;
            double offsetY = cellHeight / 2.0;

            double relX = e.getX() - offsetX;
            double relY = e.getY() - offsetY;

            int col = (int) Math.round(relX / cellWidth);
            int row = (int) Math.round(relY / cellHeight);

            col = Math.max(0, Math.min(size - 1, col));
            row = Math.max(0, Math.min(size - 1, row));

            client.send(col + " " + row);
        });
    }
}
