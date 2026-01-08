// Reprezentacja silnika gry Go
// Wzorzec: Controller
package pl.pwr.gogame.model;

import java.util.ArrayList;
import java.util.List;

public class GameEngine {

    private final Board board;
    
    //Pola przechowujące graczy
    private GamePlayer blackPlayer;
    private GamePlayer whitePlayer;
    private GamePlayer currentPlayer;

    private int blackCaptures = 0;
    private int whiteCaptures = 0;

    public GameEngine(Board board) {
        this.board = board;
        //Domyślnie null, czekamy na wywołanie setPlayers
        this.currentPlayer = null;
    }

    //inicjzalizuje graczy
    public void setPlayers(GamePlayer blackPlayer, GamePlayer whitePlayer) {
        this.blackPlayer = blackPlayer;
        this.whitePlayer = whitePlayer;
        //Zawsze zaczyna czarny
        this.currentPlayer = blackPlayer;
    }

    public void changePlayers() {
        if (currentPlayer == blackPlayer) {
            currentPlayer = whitePlayer;
        } else {
            currentPlayer = blackPlayer;
        }
    }

    public synchronized MoveResult applyMove(Move move) {
        //Sprawdź czy gracze zostali ustawieni
        if (currentPlayer == null) {
            return MoveResult.error("Gracze nie zostali zainicjalizowani!");
        }

        Position position = move.getPosition();
       
        //Sprawdza czy jest to tura gracza

        GamePlayer movePlayer = move.getPlayer();
        if(!movePlayer.equals(currentPlayer)) {
            return MoveResult.error("Tura przeciwnika");
        }

        StoneColor moveColor = movePlayer.getColor();
        // Walidacja logiki planszy
        String error = validatePreConditions(position, moveColor);
        if (error != null) {
            return MoveResult.error(error);
        }

        //Postawienie kamienia
        board.setStone(position, moveColor);

        //Próba bicia
        List<Position> capturedStones = tryCaptureOpponents(position, moveColor);

        //Samobójstwo (Suicide rule)
        if (capturedStones.isEmpty() && board.getLibertiesCount(position) == 0) {
            board.removeStone(position); // Cofnij ruch
            return MoveResult.error("Nie można postawić kamienia - samobójstwo");
        }

        //Aktualizacja punktów
        updateCaptureCounts(moveColor, capturedStones.size());

        // switch turn now that the move succeeded
        changePlayers();

        return MoveResult.ok(capturedStones);
    }

    private String validatePreConditions(Position position, StoneColor color) {
        if (color == StoneColor.EMPTY) {
            return "Nieprawidłowy kolor";
        }
        if (position == null) {
            return "Brak pozycji";
        }
        if (board.isOutOfBounds(position)) {
            return "Poza planszą";
        }
        if (!board.isEmpty(position)) {
            return "Pole zajęte";
        }
        return null;
    }

    private List<Position> tryCaptureOpponents(Position currentMovePos, StoneColor myColor) {
        List<Position> captured = new ArrayList<>();
        StoneColor opponentColor = myColor.other();

        for (Position neighbor : board.getNeighbors(currentMovePos)) {
            if (board.getStone(neighbor) == opponentColor) {
                if (board.getLibertiesCount(neighbor) == 0) {
                    board.removeStone(neighbor);
                    captured.add(neighbor);
                }
            }
        }
        return captured;
    }

    private void updateCaptureCounts(StoneColor color, int count) {
        if (count == 0) return;
        if (color == StoneColor.BLACK) {
            this.blackCaptures += count;
        } else {
            this.whiteCaptures += count;
        }
    }

    public GamePlayer getCurrentPlayer() {
        return currentPlayer;
    }

    //Metoda pomocnicza, jeśli potrzebujesz samego koloru
    public StoneColor getCurrentColor() {
        return currentPlayer != null ? currentPlayer.getColor() : StoneColor.EMPTY;
    }
    
    public int getBlackCaptures() { return blackCaptures; }
    public int getWhiteCaptures() { return whiteCaptures; }
}