package pl.pwr.gogame.client.view;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

public class GameView {
    int boardsize;
    private BorderPane root = new BorderPane();
    private BoardCanvas boardCanvas;
    private TextArea logArea = new TextArea();
    private TextField inputField = new TextField();

    public GameView() {
        logArea.setEditable(false);
        logArea.setPrefHeight(200);

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

    public void createBoard(int size) {
        BoardCanvas board = new BoardCanvas(size);
        board.setDisable(true);
        this.boardCanvas = board;
        root.setCenter(board);
    }

    
}
