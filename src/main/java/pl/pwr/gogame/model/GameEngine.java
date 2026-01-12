package pl.pwr.gogame.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pl.pwr.gogame.service.BoardService;
import pl.pwr.gogame.service.GameEngineService;

/**
 * Fasada dla logiki gry. Przechowuje stan gry i deleguje operacje do serwisów.
 */
public class GameEngine {

    //atany gry
    private final Board board;
    private GamePlayer blackPlayer;
    private GamePlayer whitePlayer;
    private GamePlayer currentPlayer;
    private int blackCaptures = 0;
    private int whiteCaptures = 0;
    private String previousBoardSnapshot;
    private boolean singleCaptureOnLastMove;
    private boolean lastMoveWasPass = false;
    private boolean end = false;

    //serwisy
    private final BoardService boardService;
    private final GameEngineService GameEngineService;

    public GameEngine(Board board) {
        this.board = board;
        this.boardService = new BoardService();
        this.GameEngineService = new GameEngineService(this.boardService);
        this.currentPlayer = null;
    }

    //metody fasady 
    public synchronized MoveResult applyMove(Move move) {
        return GameEngineService.applyMove(this, move);
    }

    public synchronized MoveResult pass(GamePlayer player) {
        return GameEngineService.pass(this, player);
    }

    public synchronized MoveResult resign(GamePlayer player) {
        return GameEngineService.resign(this, player);
    }

    //zarzadzanie graczami i punktacja
    public void setPlayers(GamePlayer blackPlayer, GamePlayer whitePlayer) {
        this.blackPlayer = blackPlayer;
        this.whitePlayer = whitePlayer;
        this.currentPlayer = blackPlayer;
    }

    public void changePlayers() {
        currentPlayer = (currentPlayer == blackPlayer) ? whitePlayer : blackPlayer;
    }

    public void updateCaptureCounts(StoneColor color, int count) {
        if (count == 0) return;
        if (color == StoneColor.BLACK) blackCaptures += count;
        else whiteCaptures += count;
    }
    
    public GamePlayer getOpponentPlayer(GamePlayer player) {
        if (player.equals(blackPlayer)) return whitePlayer;
        if (player.equals(whitePlayer)) return blackPlayer;
        throw new IllegalArgumentException("Nieznany gracz");
    }

    public ScoreResult calculateScores() {
        int blackTerritory = 0;
        int whiteTerritory = 0;
        Set<Position> visited = new HashSet<>();
        int boardSize = board.getSize();
        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                Position currentPos = new Position(c, r);
                if (board.isEmpty(currentPos) && !visited.contains(currentPos)) {
                    List<Position> region = boardService.getEmptyRegion(board, currentPos, visited);
                    Set<StoneColor> borderingColors = boardService.getBorderingColors(board, region);
                    if (borderingColors.size() == 1) {
                        if (borderingColors.contains(StoneColor.BLACK)) {
                            blackTerritory += region.size();
                        } else if (borderingColors.contains(StoneColor.WHITE)) {
                            whiteTerritory += region.size();
                        }
                    }
                }
            }
        }
        int finalBlackScore = blackTerritory + getBlackCaptures();
        int finalWhiteScore = whiteTerritory + getWhiteCaptures();
        GamePlayer winner = (finalBlackScore > finalWhiteScore) ? blackPlayer : whitePlayer;
        if (finalBlackScore == finalWhiteScore) {
            winner = null; // Remis
        }
        return new ScoreResult(finalBlackScore, finalWhiteScore, winner);
    }

    //gettery i settery
    public Board getBoard() { return board; }
    public GamePlayer getCurrentPlayer() { return currentPlayer; }
    public void setCurrentPlayer(GamePlayer player) { this.currentPlayer = player; }
    public boolean getLastMoveWasPass() { return lastMoveWasPass; }
    public void setLastMoveWasPass(boolean value) { this.lastMoveWasPass = value; }
    public boolean isEnd() { return end; }
    public void setEnd(boolean value) { this.end = value; }
    public String getPreviousBoardSnapshot() { return previousBoardSnapshot; }
    public void setPreviousBoardSnapshot(String snapshot) { this.previousBoardSnapshot = snapshot; }
    public boolean isSingleCaptureOnLastMove() { return singleCaptureOnLastMove; }
    public void setSingleCaptureOnLastMove(boolean value) { this.singleCaptureOnLastMove = value; }
    public int getBlackCaptures() { return blackCaptures; }
    public int getWhiteCaptures() { return whiteCaptures; }
    public StoneColor getCurrentColor() { return currentPlayer != null ? currentPlayer.getColor() : StoneColor.EMPTY; }
}