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

    //Poprzednią funkcję findGroup rozbiliśmy na floodfill i funkcje znajdujące
    //grupy kamieni danego koloru i funkcję szukania pustych regionów,
    //bo algorytm szukania jest ten sam
    /**
     * Znajduje grupę połączonych kamieni tego samego koloru. 
     */
    /*public void findGroup(Board board, Position startPosition, List<Position> group, Set<Position> visited) {
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
    }*/

        public List<Position> floodFill(Board board, Position startPosition, StoneColor color, Set<Position> visited) {
       
              List<Position> group = new ArrayList<>();
        //jeśli nasza pozycja startowa nie jest kolorem który sprawdzamy, lub jeśli już ją sprawdziliśmy, zwracamy pustą listę
        if (board.getStone(startPosition) != color || visited.contains(startPosition)) {
        return group;
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

        return group;
    }

   


    /**
     * Ułatwia korzystanie z findGroup, zwracając gotową listę kamieni w grupie.
     * @param startPosition Pozycja startowa.
     * @param visited Zbiór już odwiedzonych kamieni (aby nie sprawdzać tej samej grupy wielokrotnie).
     * @return Lista kamieni w grupie.
     */
    public List<Position> getGroup(Board board, Position startPosition, Set<Position> visited) {
        StoneColor color = board.getStone(startPosition);
        //ta funkcja działa tylko dla kolorów, nie dla pustych pól. Jak kolor jest pusty to
        //zwracamy pustą listę
        if (color == StoneColor.EMPTY) {
            return List.of();
        }
        return floodFill(board, startPosition, color, visited);
    }
     //Znajduje pola należące do pustego regionu.
    public List<Position> getEmptyRegion(Board board, Position start, Set<Position> visited) {
        return floodFill(board, start, StoneColor.EMPTY, visited);
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

    //Tworzy listę kolorów kamieni sąsiadujących z pustym terytorium.
    //W zasadzie odwrotność funkcji getGroupLiberties- dla pustych pól
    //sprawdzamy czy sąsiad jest kamieniem i dodajemy jego kolor do zbioru
    public Set<StoneColor> getBorderingColors(Board board, List<Position> emptyRegion) {
            Set<StoneColor> colors = new HashSet<>();

            for (Position p: emptyRegion) {
                for (Position neighbor : getNeighbors(board, p)) {
                    StoneColor c = board.getStone(neighbor);
                    if (c != StoneColor.EMPTY) {
                        colors.add(c);
                    }
                }
            }

        return colors;

    }
}