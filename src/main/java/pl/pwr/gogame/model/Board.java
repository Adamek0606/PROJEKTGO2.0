package pl.pwr.gogame.model;
// Reprezentacja planszy do gry Go
// Wzorzec: Information Expert
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

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

    /**
     * Znajduje grupę połączonych kamieni tego samego koloru. 
     */
    public void findGroup(Position startPosition, List<Position> group, Set<Position> visited) {
        StoneColor color = getStone(startPosition);
        if (color == StoneColor.EMPTY || visited.contains(startPosition)) {
            return;
        }

        Queue<Position> queue = new LinkedList<>();
        queue.add(startPosition);
        visited.add(startPosition);

        while (!queue.isEmpty()) {
            Position current = queue.poll();
            group.add(current);

            for (Position neighbor : getNeighbors(current)) {
                if (!visited.contains(neighbor) && getStone(neighbor) == color) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
    }
    
    /**
     * @param startPosition Pozycja startowa.
     * @param visited Zbiór już odwiedzonych kamieni (aby nie sprawdzać tej samej grupy wielokrotnie).
     * @return Lista kamieni w grupie.
     */
    public List<Position> getGroup(Position startPosition, Set<Position> visited) {
        List<Position> group = new ArrayList<>();
        if (!visited.contains(startPosition)) {
            findGroup(startPosition, group, visited);
        }
        return group;
    }

    /**
     * Oblicza liczbę oddechów dla całej grupy kamieni. (Istniejąca metoda)
     */
    public int getGroupLiberties(List<Position> group) {
        Set<Position> liberties = new HashSet<>();
        for (Position stone : group) {
            for (Position neighbor : getNeighbors(stone)) {
                if (isEmpty(neighbor)) {
                    liberties.add(neighbor);
                }
            }
        }
        return liberties.size();
    }
}