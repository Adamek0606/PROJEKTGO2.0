package pl.pwr.gogame.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pl.pwr.gogame.service.BoardService;

public class GameEngine {

    private final Board board;
    private final BoardService boardService; // Dodano pole dla serwisu
    private GamePlayer blackPlayer;
    private GamePlayer whitePlayer;
    private GamePlayer currentPlayer;
    private int blackCaptures = 0;
    private int whiteCaptures = 0;

    public GameEngine(Board board) {
        this.board = board;
        this.boardService = new BoardService(); // Inicjalizacja serwisu
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
            // Użycie BoardService do sprawdzenia samobójstwa
            List<Position> myGroup = boardService.getGroup(board, position, new HashSet<>());
            if (boardService.getGroupLiberties(board, myGroup) == 0) {
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

        // Użycie BoardService do pobrania sąsiadów
        for (Position neighbor : boardService.getNeighbors(board, currentMovePos)) {
            if (board.getStone(neighbor) == opponentColor && !visitedStones.contains(neighbor)) {
                // Użycie BoardService do znalezienia grupy i obliczenia oddechów
                List<Position> opponentGroup = boardService.getGroup(board, neighbor, visitedStones);
                
                if (boardService.getGroupLiberties(board, opponentGroup) == 0) {
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