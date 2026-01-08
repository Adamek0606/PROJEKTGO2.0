package pl.pwr.gogame.server;
// Formatuje odpowiedzi serwera gry Go dla klienta
// Wzorzec: Adapter
import pl.pwr.gogame.model.Board;
import pl.pwr.gogame.model.MoveResult;
import pl.pwr.gogame.model.GamePlayer;
import pl.pwr.gogame.model.StoneColor;

public class ResponseFormatter {

    public static String formatWelcome(Board board) {
        StringBuilder sb = new StringBuilder();
        sb.append("Aktualna plansza:").append(System.lineSeparator());
        sb.append(board.toString()).append(System.lineSeparator());
        sb.append("Uzycie: i j - np. '2 3' ustawi kamien na kolumnie 2, wierszu 3.");
        return sb.toString();
    }

    public static String formatMoveResult(MoveResult result) {
        if (result.isOk()) {
            if (result.getCapturedPositions().isEmpty()) {
                return "Ruch poprawny.";
            }
            return "Ruch poprawny. Zbite kamienie: " + result.getCapturedPositions().size();
        } else {
            return "BŁĄD: " + result.getErrorMessage();
        }
    }

    public static String formatStatus(GamePlayer nextPlayer, StoneColor nextColor) {
        String name = (nextPlayer != null) ? nextPlayer.getName() : "(brak)";
        return "Tura: " + name + " (" + nextColor + ")";
    }
}