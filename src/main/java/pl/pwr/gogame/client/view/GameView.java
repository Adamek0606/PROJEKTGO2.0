package pl.pwr.gogame.client.view;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class GameView {
    int boardsize = 9;
    private BorderPane root = new BorderPane();
    private BoardCanvas boardCanvas = new BoardCanvas(boardsize);
    private TextArea logArea = new TextArea();
    private TextField inputField = new TextField();
    private final Button passButton = new Button("Pass");
    private final Button resignButton = new Button("Resign");

  public GameView() {
        logArea.setEditable(false);
        logArea.setPrefHeight(100);

        HBox bottomControls = new HBox(passButton, resignButton);
        bottomControls.setSpacing(10);

        root.setCenter(boardCanvas);
        root.setBottom(bottomControls);
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
    public Button getPassButton() {
        return passButton;
    }
    public Button getResignButton() {
        return resignButton;
    }
}
