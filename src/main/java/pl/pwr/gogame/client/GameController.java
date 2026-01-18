package pl.pwr.gogame.client;

import pl.pwr.gogame.client.view.BoardCanvas;
import pl.pwr.gogame.client.view.GameView;

public class GameController {
    
    private final GameView view;
    private final NetworkClient client;
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
