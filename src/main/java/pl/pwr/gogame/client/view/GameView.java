package pl.pwr.gogame.client.view;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class GameView {
    int boardsize;
    private BorderPane root = new BorderPane();
    private BoardCanvas boardCanvas;
    private TextArea logArea = new TextArea();
    private Button passButton = new Button("Pass");
    private Button resignButton = new Button("Resign");

    public GameView() {
        logArea.setEditable(false);
        logArea.setPrefHeight(200);

    HBox bottom = new HBox(8);
    bottom.setPadding(new Insets(6));
    bottom.getChildren().addAll(passButton, resignButton);

    root.setBottom(bottom);
    root.setTop(logArea);

    }

    //getters
    public Parent getRoot() {
        return root;
    }

    public BoardCanvas getBoardCanvas() {
        return boardCanvas;
    }

    public TextArea getLogArea() {
        return logArea;
    }

    public Button getPassButton() {
        return passButton;
    }

    public Button getResignButton() {
        return resignButton;
    }

    public void createBoard(int size) {
        BoardCanvas board = new BoardCanvas(size);
        board.setDisable(true);
        this.boardCanvas = board;
        root.setCenter(board);
    }

}
