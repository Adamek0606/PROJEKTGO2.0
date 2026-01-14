package pl.pwr.gogame.client;

import pl.pwr.gogame.client.view.GameView;

public class GameController {
    
    private final GameView view;
    private final NetworkClient client;
   public GameController(GameView view) {
    this.view = view;
    this.client = new NetworkClient(
        "localhost",
        58901,
        view.getBoardCanvas(), view.getLogArea()
    );

    initHandlers();
}

    private void initHandlers() {
        view.getBoardCanvas().setOnMouseClicked(e -> {
            int col = (int) ((e.getX() - 20) / 40);
            int row = (int) ((e.getY() - 20) / 40);
            client.send(col + " " + row);


        });
         view.getPassButton().setOnAction(e -> {
            client.send("PASS");
        });

        view.getResignButton().setOnAction(e -> {
            client.send("RESIGN");
        });
    }
}
