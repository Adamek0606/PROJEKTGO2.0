package pl.pwr.gogame.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import pl.pwr.gogame.model.Board;
import pl.pwr.gogame.model.Position;
import pl.pwr.gogame.model.StoneColor;

/**
 * Serwis zawierający logikę operującą na obiekcie Board.
 */
public class BoardService {

   public List<Position> getNeighbors(Board board, Position position) {
    List<Position> neighbors = new ArrayList<>();
    int[] dRow = {-1, 1, 0, 0}; 
    int[] dCol = {0, 0, -1, 1}; 

    for (int i = 0; i < 4; i++) {
        // POPRAWIONA KOLEJNOŚĆ: (kolumna, wiersz)
        Position neighbor = new Position(position.col() + dCol[i], position.row() + dRow[i]);
        if (!board.isOutOfBounds(neighbor)) {
            neighbors.add(neighbor);
        }
    }
    return neighbors;
}
    public int getLibertiesCount(Board board, Position position) {
        int count = 0;
        for (Position neighbor : getNeighbors(board, position)) {
            if (board.getStone(neighbor) == StoneColor.EMPTY) {
                count++;
            }
        }
        return count;
    }

    /**
     * Znajduje grupę połączonych kamieni tego samego koloru. 
     */
    public void findGroup(Board board, Position startPosition, List<Position> group, Set<Position> visited) {
        StoneColor color = board.getStone(startPosition);
        if (color == StoneColor.EMPTY || visited.contains(startPosition)) {
            return;
        }

        Queue<Position> queue = new LinkedList<>();
        queue.add(startPosition);
        visited.add(startPosition);

        while (!queue.isEmpty()) {
            Position current = queue.poll();
            group.add(current);

            for (Position neighbor : getNeighbors(board, current)) {
                if (!visited.contains(neighbor) && board.getStone(neighbor) == color) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
    }

    /**
     * Ułatwia korzystanie z findGroup, zwracając gotową listę kamieni w grupie.
     * @param startPosition Pozycja startowa.
     * @param visited Zbiór już odwiedzonych kamieni (aby nie sprawdzać tej samej grupy wielokrotnie).
     * @return Lista kamieni w grupie.
     */
    public List<Position> getGroup(Board board, Position startPosition, Set<Position> visited) {
        List<Position> group = new ArrayList<>();
        if (board.getStone(startPosition) != StoneColor.EMPTY && !visited.contains(startPosition)) {
            findGroup(board, startPosition, group, visited);
        }
        return group;
    }

    /**
     * Oblicza liczbę oddechów dla całej grupy kamieni. (Istniejąca metoda)
     */
    public int getGroupLiberties(Board board, List<Position> group) {
        Set<Position> liberties = new HashSet<>();
        for (Position stone : group) {
            for (Position neighbor : getNeighbors(board, stone)) {
                if (board.isEmpty(neighbor)) {
                    liberties.add(neighbor);
                }
            }
        }
        return liberties.size();
    }
}