package pl.pwr.gogame.client.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import pl.pwr.gogame.model.StoneColor;

import java.util.HashMap;
import java.util.Map;
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

        widthProperty().addListener(evt -> redraw());
        heightProperty().addListener(evt -> redraw());
        
        drawEmptyBoard();
    }

    // expose size so controllers can map mouse coordinates to intersections
    public int getSize() {
        return size;
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
        double w = getWidth();
        double h = getHeight();

        double cellWidth = w / size;
        double cellHeight = h / size;

 
        for (int i = 0; i < size; i++) {
            
            gc.strokeLine(cellWidth / 2, cellHeight / 2 + i * cellHeight,
                    w - cellWidth / 2, cellHeight / 2 + i * cellHeight);
           
            gc.strokeLine(cellWidth / 2 + i * cellWidth, cellHeight / 2,
                    cellWidth / 2 + i * cellWidth, h - cellHeight / 2);
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
    double w = getWidth();
        double h = getHeight();
        double cellWidth = w / size;
        double cellHeight = h / size;
        double stoneDiameter = Math.min(cellWidth, cellHeight) * 0.7; // 70% of cell

        for (Map.Entry<String, StoneColor> entry : stones.entrySet()) {
            String[] parts = entry.getKey().split(",");
            int col = Integer.parseInt(parts[0]);
            int row = Integer.parseInt(parts[1]);

            gc.setFill(entry.getValue() == StoneColor.BLACK ? Color.BLACK : Color.WHITE);

            double x = col * cellWidth + cellWidth / 2 - stoneDiameter / 2;
            double y = row * cellHeight + cellHeight / 2 - stoneDiameter / 2;

            gc.fillOval(x, y, stoneDiameter, stoneDiameter);
        }
    }
}
