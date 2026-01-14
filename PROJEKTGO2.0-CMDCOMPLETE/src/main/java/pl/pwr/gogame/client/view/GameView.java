package pl.pwr.gogame.client.view;

import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

public class GameView {
    int boardsize = 9;
    private BorderPane root = new BorderPane();
    private BoardCanvas boardCanvas = new BoardCanvas(boardsize);
    private TextArea logArea = new TextArea();
    private TextField inputField = new TextField();

    public GameView() {
        logArea.setEditable(false);
        logArea.setPrefHeight(200);

        root.setCenter(boardCanvas);
        root.setBottom(inputField);
        root.setTop(logArea);

    }

    //gettersy
    public Parent getRoot() {
        return root;
    }

    public BoardCanvas getBoardCanvas() {
        return boardCanvas;
    }

    public TextArea getLogArea() {
        return logArea;
    }

    public TextField getInputField() {
        return inputField;
    }
}
