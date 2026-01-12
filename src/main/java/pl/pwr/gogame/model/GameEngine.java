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

    //potrzebne do sprawdzania ko
    private String previousBoardSnapshot;
    private boolean singleCaptureOnLastMove;

    //ten kod do passowania będzie można zamienić po zaimplementowaniu historii ruchów
    private boolean lastMoveWasPass = false;

    private boolean end = false;

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

         if (end) {
        return MoveResult.error("Gra została zakończona");
        }

        if (currentPlayer == null) {
            return MoveResult.error("Gracze nie zostali zainicjalizowani!");
        }


        Position position = move.getPosition();
        GamePlayer movePlayer = move.getPlayer();

        //jeśli ruch to nie pass to przy następnym sprawdzaniu poprzedni
        //ruch nie będzie pass
        lastMoveWasPass = false;

       

        if (!movePlayer.equals(currentPlayer)) {
            return MoveResult.error("Tura przeciwnika");
        }
        

        StoneColor moveColor = movePlayer.getColor();
        String error = validatePreConditions(position, moveColor);
        if (error != null) {
            return MoveResult.error(error);
        }

         //stan planszy przed wykonaniem ruchu w celu sprawdzenia ko
        String previousBoardState = board.toString();

        board.setStone(position, moveColor);
        List<Position> capturedStones = tryCaptureOpponents(position, moveColor);

        //ko może zajść tylko wtedy, gdy gracz przejmuje tylko jeden kamień
        boolean singleCapture = false;
        if (capturedStones.size() == 1) {
            singleCapture = true;
        }

        if (capturedStones.isEmpty()) {
            // Użycie BoardService do sprawdzenia samobójstwa
            List<Position> myGroup = boardService.getGroup(board, position, new HashSet<>());
            if (boardService.getGroupLiberties(board, myGroup) == 0) {
                board.removeStone(position); // Cofnij ruch
                return MoveResult.error("Nie można postawić kamienia - samobójstwo");
            }
        }

        if (singleCapture && singleCaptureOnLastMove) {
            String newBoardState = board.toString();

            if(newBoardState.equals(previousBoardSnapshot)) {
                //ponieważ sprawdzamy czy zaszło ko po wykonaniu ruchu,
                //jeśli zaszło należy cofnąć ten ruch
                rollbackMove(position, capturedStones, moveColor);
                return MoveResult.error("Zaszło ko- ruch nieprawidłowy");
            }
        }

        previousBoardSnapshot = previousBoardState;
        singleCaptureOnLastMove = singleCapture;

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

    public synchronized MoveResult pass(GamePlayer player) {
        if (!player.equals(currentPlayer)) {
            return MoveResult.error("Tura przeciwnika");
        }

        //przy passowaniu ko resetuje się
        singleCaptureOnLastMove = false;
        previousBoardSnapshot = null;

         if (lastMoveWasPass) {
            end = true;
            return MoveResult.passEnd();
        } else {
            lastMoveWasPass = true;
            changePlayers();
            return MoveResult.passNext();
        }
    }

    public synchronized MoveResult resign(GamePlayer player) {

        //gdybyśmy chcieli możliwość poddania się tylko w swojej turze
        /*if (!player.equals(currentPlayer)) {
            return MoveResult.error("Tura przeciwnika");

        }*/

        GamePlayer winner = (player.equals(blackPlayer)) ? whitePlayer : blackPlayer ;

        this.currentPlayer = null;

        return MoveResult.resign(player, winner);
    }

    public GamePlayer getCurrentPlayer() { return currentPlayer; }
    private GamePlayer getOpponentPlayer(GamePlayer player) {
    if (player.equals(blackPlayer)) {
        return whitePlayer;
    }
    if (player.equals(whitePlayer)) {
        return blackPlayer;
    }
    throw new IllegalArgumentException("Nieznany gracz");
}

    //funkcja do cofania ruchu- usuwa postawiony kamień i przywraca złapane kamienie
    private void rollbackMove(Position placed, List<Position> captured, StoneColor color) {
        board.removeStone(placed);
        for (Position p : captured) {
            board.setStone(p, color.other());
        }

    }


    public StoneColor getCurrentColor() { return currentPlayer != null ? currentPlayer.getColor() : StoneColor.EMPTY; }
    public int getBlackCaptures() { return blackCaptures; }
    public int getWhiteCaptures() { return whiteCaptures; }
    public boolean getLastMoveWasPass() { return lastMoveWasPass; }

    public ScoreResult calculateScores() {
    int blackTerritory = 0;
    int whiteTerritory = 0;

    Set<Position> visited = new HashSet<>();
    int boardSize = board.getSize();

    // Przechodzimy przez całą planszę, aby znaleźć puste regiony
    for (int r = 0; r < boardSize; r++) {
        for (int c = 0; c < boardSize; c++) {
            Position currentPos = new Position(c, r);
            if (board.isEmpty(currentPos) && !visited.contains(currentPos)) {
                // Znajdź cały pusty region
                List<Position> region = boardService.getEmptyRegion(board, currentPos, visited);
                
                // Sprawdź, jakie kolory otaczają ten region
                Set<StoneColor> borderingColors = boardService.getBorderingColors(board, region);

                // Jeśli region jest otoczony tylko przez jeden kolor, należy do tego gracza
                if (borderingColors.size() == 1) {
                    if (borderingColors.contains(StoneColor.BLACK)) {
                        blackTerritory += region.size();
                    } else if (borderingColors.contains(StoneColor.WHITE)) {
                        whiteTerritory += region.size();
                    }
                }
                // Jeśli region jest otoczony przez oba kolory lub żaden, jest neutralny (dame)
            }
        }
    }

    // Końcowy wynik = terytorium + zbite kamienie
    int finalBlackScore = blackTerritory + getBlackCaptures();
    int finalWhiteScore = whiteTerritory + getWhiteCaptures();

    GamePlayer winner = (finalBlackScore > finalWhiteScore) ? blackPlayer : whitePlayer;
    
    // Obsługa remisu - w Go rzadkie, ale możliwe. Można tu zaimplementować komi.
    if (finalBlackScore == finalWhiteScore) {
        winner = null; // Remis
    }

    return new ScoreResult(finalBlackScore, finalWhiteScore, winner);
}
}