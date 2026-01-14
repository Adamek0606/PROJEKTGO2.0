// reprezentacja ruchu w grze Go
// Wzorzec:
package pl.pwr.gogame.model;

public class Move {
    private final Position position;
    private GamePlayer player;
    public Move(Position position, GamePlayer player) {
        if (position == null) {
            throw new IllegalArgumentException("Position cannot be null");
        }
        this.position = position;
        this.player = player;
    }

    public Position getPosition() { return position; }
    public GamePlayer getPlayer() { return player; }
}
