package pl.pwr.gogame.model;
// Reprezentacja planszy do gry Go
// Wzorzec: Information Expert
import java.util.ArrayList;
import java.util.List;

public class Board {
    private final int size;
    private final StoneColor[][] grid;

    Board(int size) {
        this.size = size;
    this.grid = new StoneColor[size][size];
        initialize();
    }

    private void initialize() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j] = StoneColor.EMPTY;
            }
        }
    }

    public StoneColor getStone(Position position) {
        if (isOutOfBounds(position)) throw new IllegalArgumentException("Poza planszą");
        return grid[position.row()][position.col()];
    }

    public void setStone(Position position, StoneColor stone) {
        if (!isOutOfBounds(position)) grid[position.row()][position.col()] = stone;
    }

    public void removeStone(Position position) {
    setStone(position, StoneColor.EMPTY);
    }

    public int getSize() { return size; }

    public boolean isOutOfBounds(Position position) {
        return position.row() < 0 || position.row() >= size || position.col() < 0 || position.col() >= size;
    }

    public boolean isEmpty(Position position) {
        if (isOutOfBounds(position)) return false;
    return grid[position.row()][position.col()] == StoneColor.EMPTY;
    }

    public List<Position> getNeighbors(Position position) {
        List<Position> neighbors = new ArrayList<>();
        // Tablice przesunięć dla: góra, dół, lewo, prawo
        int[] dRow = {-1, 1, 0, 0};
        int[] dCol = {0, 0, -1, 1};

        for (int i = 0; i < 4; i++) {
            Position neighbor = new Position(position.col() + dCol[i], position.row() + dRow[i]);
            if (!isOutOfBounds(neighbor)) {
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }

    public int getLibertiesCount(Position position) {
        int count = 0;
        for (Position neighbor : getNeighbors(position)) {
            if (getStone(neighbor) == StoneColor.EMPTY) {
                count++;
            }
        }
        return count;
    }

    @Override
    public String toString() {
        //lineSeparator printuje nam /n poprawnie w kazdym rodzaju terminala
        String ls = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append("   ");
        for (int c = 0; c < size; c++) sb.append(String.format(" %2d", c));
        sb.append(ls);
        for (int r = 0; r < size; r++) {
            sb.append(String.format("%2d ", r));
            for (int c = 0; c < size; c++) {
                StoneColor p = grid[r][c];
                char ch = (p == StoneColor.BLACK) ? 'B' : (p == StoneColor.WHITE) ? 'W' : '.';
                sb.append(String.format("  %c", ch));
            }
            sb.append(ls);
        }
        return sb.toString();
    }
}