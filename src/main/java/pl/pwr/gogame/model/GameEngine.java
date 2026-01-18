package pl.pwr.gogame.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pl.pwr.gogame.service.BoardService;
import pl.pwr.gogame.service.GameEngineService;

/**
 * Klasa {@code GameEngine} pełni rolę fasady dla logiki gry Go.
 * Przechowuje aktualny stan gry (planszę, graczy, punktację)
 * oraz deleguje szczegółowe operacje do odpowiednich serwisów.
 */
/**
 * Fasada dla logiki gry. Przechowuje stan gry i deleguje operacje do serwisów.
 */
public class GameEngine {

    //atany gry

    /**
     * Plansza gry.
     */
    private final Board board;

    /**
     * Gracz grający kolorem czarnym.
     */
    private GamePlayer blackPlayer;

    /**
     * Gracz grający kolorem białym.
     */
    private GamePlayer whitePlayer;

    /**
     * Aktualny gracz wykonujący ruch.
     */
    private GamePlayer currentPlayer;

    /**
     * Liczba kamieni zbitych przez czarnego gracza.
     */
    private int blackCaptures = 0;

    /**
     * Liczba kamieni zbitych przez białego gracza.
     */
    private int whiteCaptures = 0;

    /**
     * Migawka poprzedniego stanu planszy, wykorzystywana
     * m.in. do sprawdzania reguły ko.
     */
    private String previousBoardSnapshot;

    /**
     * Informacja, czy w poprzednim ruchu zbity został dokładnie jeden kamień.
     */
    private boolean singleCaptureOnLastMove;

    /**
     * Informacja, czy ostatni ruch był pasem.
     */
    private boolean lastMoveWasPass = false;

    /**
     * Informacja, czy gra została zakończona.
     */
    private boolean end = false;

    //serwisy

    /**
     * Serwis operujący na planszy.
     */
    private final BoardService boardService;

    /**
     * Serwis realizujący główną logikę silnika gry.
     */
    private final GameEngineService GameEngineService;

    /**
     * Tworzy nowy silnik gry dla podanej planszy.
     *
     * @param board plansza gry
     */
    public GameEngine(Board board) {
        this.board = board;
        this.boardService = new BoardService();
        this.GameEngineService = new GameEngineService(this.boardService);
        this.currentPlayer = null;
    }

    //metody fasady 

    /**
     * Wykonuje ruch gracza i aktualizuje stan gry.
     *
     * @param move ruch do wykonania
     * @return rezultat ruchu
     */
    public synchronized MoveResult applyMove(Move move) {
        return GameEngineService.applyMove(this, move);
    }

    /**
     * Obsługuje wykonanie ruchu typu „pass” przez gracza.
     *
     * @param player gracz wykonujący pas
     * @return rezultat operacji
     */
    public synchronized MoveResult pass(GamePlayer player) {
        return GameEngineService.pass(this, player);
    }

    /**
     * Obsługuje rezygnację gracza z gry.
     *
     * @param player gracz rezygnujący
     * @return rezultat operacji
     */
    public synchronized MoveResult resign(GamePlayer player) {
        return GameEngineService.resign(this, player);
    }

    //zarzadzanie graczami i punktacja

    /**
     * Ustawia graczy gry i inicjalizuje aktualnego gracza
     * jako gracza czarnego.
     *
     * @param blackPlayer gracz czarny
     * @param whitePlayer gracz biały
     */
    public void setPlayers(GamePlayer blackPlayer, GamePlayer whitePlayer) {
        this.blackPlayer = blackPlayer;
        this.whitePlayer = whitePlayer;
        this.currentPlayer = blackPlayer;
    }

    /**
     * Zmienia aktualnego gracza na przeciwnika.
     */
    public void changePlayers() {
        currentPlayer = (currentPlayer == blackPlayer) ? whitePlayer : blackPlayer;
    }

    /**
     * Aktualizuje liczbę zbitych kamieni dla danego koloru.
     *
     * @param color kolor gracza
     * @param count liczba zbitych kamieni
     */
    public void updateCaptureCounts(StoneColor color, int count) {
        if (count == 0) return;
        if (color == StoneColor.BLACK) blackCaptures += count;
        else whiteCaptures += count;
    }

    /**
     * Zwraca przeciwnika podanego gracza.
     *
     * @param player gracz
     * @return przeciwnik gracza
     * @throws IllegalArgumentException jeśli gracz nie jest znany
     */
    public GamePlayer getOpponentPlayer(GamePlayer player) {
        if (player.equals(blackPlayer)) return whitePlayer;
        if (player.equals(whitePlayer)) return blackPlayer;
        throw new IllegalArgumentException("Nieznany gracz");
    }

    /**
     * Oblicza końcowe wyniki gry na podstawie terytoriów
     * oraz liczby zbitych kamieni.
     *
     * @return wynik punktowy gry
     */
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

    public StoneColor getCurrentColor() {
        return currentPlayer != null ? currentPlayer.getColor() : StoneColor.EMPTY;
    }
}
