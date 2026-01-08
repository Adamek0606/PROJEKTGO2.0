package pl.pwr.gogame.model;
// Reprezentacja koloru kamienia w grze Go
// Wzorzecz: Enum
public enum StoneColor {
    EMPTY, BLACK, WHITE;

    public StoneColor other() {
        if (this == BLACK) return WHITE;
        if (this == WHITE) return BLACK;
        return EMPTY;
    }
}
