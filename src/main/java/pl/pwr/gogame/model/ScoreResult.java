package pl.pwr.gogame.model;

public class ScoreResult {
    private final int blackScore;
    private final int whiteScore;
    private final GamePlayer winner;

    public ScoreResult(int blackScore, int whiteScore, GamePlayer winner) {
        this.blackScore = blackScore;
        this.whiteScore = whiteScore;
        this.winner = winner;
    }

    public int getBlackScore() { return blackScore; }
    public int getWhiteScore() { return whiteScore; }
    public GamePlayer getWinner() { return winner; }
}