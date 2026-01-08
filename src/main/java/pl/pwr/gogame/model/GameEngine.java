package pl.pwr.gogame.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameEngine {

    private final Board board;
    private GamePlayer blackPlayer;
    private GamePlayer whitePlayer;
    private GamePlayer currentPlayer;
    private int blackCaptures = 0;
    private int whiteCaptures = 0;

    public GameEngine(Board board) {
        this.board = board;
        this.currentPlayer = null;
    }

    public void setPlayers(GamePlayer blackPlayer, GamePlayer whitePlayer) {
        this.blackPlayer = blackPlayer;
        this.whitePlayer = whitePlayer;
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
        if (currentPlayer == null) {
            return MoveResult.error("Gracze nie zostali zainicjalizowani!");
        }

        Position position = move.getPosition();
        GamePlayer movePlayer = move.getPlayer();
        if (!movePlayer.equals(currentPlayer)) {
            return MoveResult.error("Tura przeciwnika");
        }

        StoneColor moveColor = movePlayer.getColor();
        String error = validatePreConditions(position, moveColor);
        if (error != null) {
            return MoveResult.error(error);
        }

        board.setStone(position, moveColor);
        List<Position> capturedStones = tryCaptureOpponents(position, moveColor);

        if (capturedStones.isEmpty()) {
            // POPRAWKA: Użycie metod z Board do sprawdzenia samobójstwa
            List<Position> myGroup = board.getGroup(position, new HashSet<>());
            if (board.getGroupLiberties(myGroup) == 0) {
                board.removeStone(position); // Cofnij ruch
                return MoveResult.error("Nie można postawić kamienia - samobójstwo");
            }
        }

        updateCaptureCounts(moveColor, capturedStones.size());
        changePlayers();
        return MoveResult.ok(capturedStones);
    }

    private String validatePreConditions(Position position, StoneColor color) {
        if (color == StoneColor.EMPTY) return "Kolor nie może być pusty";
        if (position == null) return "Pozycja nie może być pusta";
        if (board.isOutOfBounds(position)) return "Ruch poza planszą";
        if (!board.isEmpty(position)) return "Pole jest już zajęte";
        return null;
    }

    private List<Position> tryCaptureOpponents(Position currentMovePos, StoneColor myColor) {
        List<Position> allCapturedStones = new ArrayList<>();
        StoneColor opponentColor = myColor.other();
        Set<Position> visitedStones = new HashSet<>();

        for (Position neighbor : board.getNeighbors(currentMovePos)) {
            if (board.getStone(neighbor) == opponentColor && !visitedStones.contains(neighbor)) {
                // POPRAWKA: Użycie nowej metody getGroup i istniejącej getGroupLiberties
                List<Position> opponentGroup = board.getGroup(neighbor, visitedStones);
                
                if (board.getGroupLiberties(opponentGroup) == 0) {
                    allCapturedStones.addAll(opponentGroup);
                }
            }
        }

        for (Position captured : allCapturedStones) {
            board.removeStone(captured);
        }
        return allCapturedStones;
    }

    private void updateCaptureCounts(StoneColor color, int count) {
        if (count == 0) return;
        if (color == StoneColor.BLACK) {
            blackCaptures += count;
        } else {
            whiteCaptures += count;
        }
    }

    public GamePlayer getCurrentPlayer() { return currentPlayer; }
    public StoneColor getCurrentColor() { return currentPlayer != null ? currentPlayer.getColor() : StoneColor.EMPTY; }
    public int getBlackCaptures() { return blackCaptures; }
    public int getWhiteCaptures() { return whiteCaptures; }
}