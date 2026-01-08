package pl.pwr.gogame.model;
// reprezentacja wyniku ruchu w grze Go
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MoveResult {
    
    private final boolean ok;
    private final List<Position> capturedPositions;
    private final String errorMessage;

    public MoveResult(boolean ok, String errorMessage, List<Position> capturedPositions) {
        this.ok = ok;
        this.capturedPositions = capturedPositions != null ? capturedPositions : new ArrayList<>();
        this.errorMessage = errorMessage;

    }


    public static MoveResult ok(List<Position> capturedPositions) {
        return new MoveResult(true, null, capturedPositions);

    }

    public static MoveResult error(String message) {
        return new MoveResult(false, message, Collections.emptyList());
    }

    public boolean isOk() { return ok; }
    public List<Position> getCapturedPositions() { return capturedPositions; }

    public String getErrorMessage() { return this.errorMessage; }
    
}