package pl.pwr.gogame.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pl.pwr.gogame.model.Board;
import pl.pwr.gogame.model.GameEngine;
import pl.pwr.gogame.model.GamePlayer;
import pl.pwr.gogame.model.Move;
import pl.pwr.gogame.model.MoveResult;
import pl.pwr.gogame.model.Position;
import pl.pwr.gogame.model.StoneColor;

/**
 * Serwis zawierający główną logikę gry.
 */
public class GameEngineService {

    private final BoardService boardService;
    
    public GameEngineService(BoardService boardService) {
        this.boardService = boardService;
    }

    /**
     * Stosuje ruch na planszy, waliduje go i zarządza stanem gry.
     */
    public MoveResult applyMove(GameEngine engine, Move move) {
        if (engine.isEnd()) {
            return MoveResult.error("Gra została zakończona");
        }
        if (engine.getCurrentPlayer() == null) {
            return MoveResult.error("Gracze nie zostali zainicjalizowani!");
        }

        engine.setLastMoveWasPass(false);
        
        if (!move.getPlayer().equals(engine.getCurrentPlayer())) {
            return MoveResult.error("Tura przeciwnika");
        }

        String error = validatePreConditions(engine.getBoard(), move.getPosition(), move.getPlayer().getColor());
        if (error != null) {
            return MoveResult.error(error);
        }

        String previousBoardState = engine.getBoard().toString();
        engine.getBoard().setStone(move.getPosition(), move.getPlayer().getColor());

        List<Position> capturedStones = tryCaptureOpponents(engine, move.getPosition(), move.getPlayer().getColor());

        boolean singleCapture = capturedStones.size() == 1;

        if (capturedStones.isEmpty()) {
            List<Position> myGroup = boardService.getGroup(engine.getBoard(), move.getPosition(), new HashSet<>());
            if (boardService.getGroupLiberties(engine.getBoard(), myGroup) == 0) {
                engine.getBoard().removeStone(move.getPosition()); // Cofnij ruch
                return MoveResult.error("Nie można postawić kamienia - samobójstwo");
            }
        }

        if (singleCapture && engine.isSingleCaptureOnLastMove()) {
            if (engine.getBoard().toString().equals(engine.getPreviousBoardSnapshot())) {
                rollbackMove(engine, move.getPosition(), capturedStones, move.getPlayer().getColor());
                return MoveResult.error("Zaszło ko- ruch nieprawidłowy");
            }
        }

        engine.setPreviousBoardSnapshot(previousBoardState);
        engine.setSingleCaptureOnLastMove(singleCapture);
        engine.updateCaptureCounts(move.getPlayer().getColor(), capturedStones.size());
        engine.changePlayers();
        return MoveResult.ok(capturedStones);
    }

    /**
     * Obsługuje spasowanie przez gracza.
     */
    public MoveResult pass(GameEngine engine, GamePlayer player) {
        if (!player.equals(engine.getCurrentPlayer())) {
            return MoveResult.error("Tura przeciwnika");
        }
        engine.setSingleCaptureOnLastMove(false);
        engine.setPreviousBoardSnapshot(null);

        if (engine.getLastMoveWasPass()) {
            engine.setEnd(true);
            return MoveResult.passEnd();
        } else {
            engine.setLastMoveWasPass(true);
            engine.changePlayers();
            return MoveResult.passNext();
        }
    }

    /**
     * Obsługuje poddanie się gracza.
     */
    public MoveResult resign(GameEngine engine, GamePlayer player) {
        GamePlayer winner = engine.getOpponentPlayer(player);
        engine.setCurrentPlayer(null); // Blokuje dalsze ruchy
        engine.setEnd(true);
        return MoveResult.resign(player, winner);
    }

    private String validatePreConditions(Board board, Position position, StoneColor color) {
        if (color == StoneColor.EMPTY) return "Kolor nie może być pusty";
        if (position == null) return "Pozycja nie może być pusta";
        if (board.isOutOfBounds(position)) return "Ruch poza planszą";
        if (!board.isEmpty(position)) return "Pole jest już zajęte";
        return null;
    }

    private List<Position> tryCaptureOpponents(GameEngine engine, Position currentMovePos, StoneColor myColor) {
        List<Position> allCapturedStones = new ArrayList<>();
        StoneColor opponentColor = myColor.other();
        Set<Position> visitedStones = new HashSet<>();

        for (Position neighbor : boardService.getNeighbors(engine.getBoard(), currentMovePos)) {
            if (engine.getBoard().getStone(neighbor) == opponentColor && !visitedStones.contains(neighbor)) {
                List<Position> opponentGroup = boardService.getGroup(engine.getBoard(), neighbor, visitedStones);
                if (boardService.getGroupLiberties(engine.getBoard(), opponentGroup) == 0) {
                    allCapturedStones.addAll(opponentGroup);
                }
            }
        }
        for (Position captured : allCapturedStones) {
            engine.getBoard().removeStone(captured);
        }
        return allCapturedStones;
    }

    private void rollbackMove(GameEngine engine, Position placed, List<Position> captured, StoneColor color) {
        engine.getBoard().removeStone(placed);
        for (Position p : captured) {
            engine.getBoard().setStone(p, color.other());
        }
    }
}