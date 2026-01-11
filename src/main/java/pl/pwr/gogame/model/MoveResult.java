package pl.pwr.gogame.model;
// reprezentacja wyniku ruchu w grze Go
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MoveResult {
    
    private final boolean ok;
    private final List<Position> capturedPositions;
    private final String errorMessage;

    private final boolean passed;
    private final boolean end;

    private final boolean resigned;
    private final GamePlayer winner;
    private final GamePlayer loser;

    public MoveResult(boolean ok, String errorMessage, List<Position> capturedPositions, boolean passed, boolean end, boolean resigned, GamePlayer winner, GamePlayer loser) {
        this.ok = ok;
        this.capturedPositions = capturedPositions != null ? capturedPositions : new ArrayList<>();
        this.errorMessage = errorMessage;
        this.passed = passed;
        this.end = end;
        this.resigned = resigned;
        this.winner = winner;
        this.loser = loser;

    }


   public static MoveResult ok(List<Position> capturedPositions) {
    return new MoveResult(true, null, capturedPositions,
            false, false, false, null, null);
}
    //pierwszy gracz zrobił pass
    public static MoveResult passNext() {
        return new MoveResult(true, null, null,
                true, false, false, null, null);
    }

    //obojga graczy zrobiło pass
    public static MoveResult passEnd() {
        return new MoveResult(true, null, null,
                true, true, false, null, null);
    }

    public static MoveResult resign(GamePlayer loser, GamePlayer winner) {
        return new MoveResult(false, null, null,
                false, true, true, winner, loser);
    }

    public static MoveResult error(String message) {
        return new MoveResult(false, message, Collections.emptyList(),
                false, false, false, null, null);
    }

    public boolean isOk() { return ok; }
    public List<Position> getCapturedPositions() { return capturedPositions; }

    public String getErrorMessage() { return this.errorMessage; }

    public boolean isPassed() { return passed; }
    public boolean isEnd() { return end; }
    public boolean isResigned() { return resigned; }
    public GamePlayer getWinner()  { return winner; }
    public GamePlayer getLoser() { return loser; }

}