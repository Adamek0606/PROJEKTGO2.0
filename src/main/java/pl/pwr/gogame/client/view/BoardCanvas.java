package pl.pwr.gogame.client.view;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import pl.pwr.gogame.model.StoneColor;
//klasa Canvas daje nam obraz na którym można rysować za pomocą
//różnych dostępnych metod
public class BoardCanvas extends Canvas {
    
    private int size;
    private int gridSize = 40;

    //żeby móc usuwać kamień z pola, należy przechowywać aktualny stan planszy,
    //ponieważ sam obiekt klasy GraphicsContext nie przechowuje historii
    //wywołanych metod i "nie wie", gdzie narysowaliśmy wcześniej kamień
    private final Map<String, StoneColor> stones = new HashMap<>();

    //konstruktor- tworzenie obrazu o danym rozmiarze
    public BoardCanvas(int size) {
        this.size = size;
        setWidth(size * gridSize);
        setHeight(size * gridSize);
        drawEmptyBoard();
    }


    public void drawEmptyBoard() {
        //każda kanwa z Canvas ma przydzielony obiekt GraphicsContext,
        //na którym wywołujemy nasze metody do rysowania. Obiekt
        //gc następnie dodaje do buffera parametry potrzebne do narysowania
        //danej rzeczy na kanwie
        GraphicsContext gc = getGraphicsContext2D();
        //czyścimy kanwę
        gc.clearRect(0, 0, getWidth(), getHeight());

        //rysujemy planszę- linie horyzontalne i wertykalne
        for (int i = 0; i < size; i++) {
            gc.strokeLine(
                gridSize / 2, gridSize / 2 + i * gridSize,
                gridSize / 2 + (size - 1) * gridSize, gridSize / 2 + i * gridSize
            );
        

            gc.strokeLine(
                    gridSize / 2 + i * gridSize, gridSize / 2,
                    gridSize / 2 + i * gridSize, gridSize / 2 + (size - 1) * gridSize
                );

        }
    }
    //rysujemy umieszczony kamień
    public void drawStone(int col, int row, StoneColor color) {
    stones.put(col + "," + row, color);
    redraw();
    }

    public void removeStone(int col, int row) {
    stones.remove(col + "," + row);
    redraw();
    }

    //za każdym razem gdy chcemy narysować lub usunąć kamień na planszy GUI,
    //rysujemy od nowa całą planszę. 
    private void redraw() {
    drawEmptyBoard();

    GraphicsContext gc = getGraphicsContext2D();
    for (Map.Entry<String, StoneColor> entry : stones.entrySet()) {
        String[] parts = entry.getKey().split(",");
        int col = Integer.parseInt(parts[0]);
        int row = Integer.parseInt(parts[1]);

        gc.setFill(entry.getValue() == StoneColor.BLACK ? Color.BLACK : Color.WHITE);
        gc.fillOval(
            col * gridSize + gridSize / 2 - 15,
            row * gridSize + gridSize / 2 - 15,
            30, 30
        );
    }
}
}
