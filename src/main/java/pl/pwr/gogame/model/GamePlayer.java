// Reprezentacja gracza w grze Go
// Wzorzec: Entity(klas modelu)
package pl.pwr.gogame.model;

public class GamePlayer {
    private final String name;
    private final StoneColor color;

    public GamePlayer(String name, StoneColor color) {
        this.name = (name == null || name.isBlank()) ? "Player" : name;
        this.color = color == null ? StoneColor.EMPTY : color;
    }

    public String getName() {
        return name;
    }

    public StoneColor getColor() {
        return color;
    }
    
    @Override
    public String toString() {
        return name + "(" + color + ")";
    }
}
