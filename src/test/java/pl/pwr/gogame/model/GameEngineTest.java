package pl.pwr.gogame.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameEngineTest {

    private Board board;
    private GameEngine gameEngine;
    private GamePlayer blackPlayer;
    private GamePlayer whitePlayer;

    @BeforeEach
    void setUp() {
        // Inicjalizujemy świeżą planszę i silnik gry przed każdym testem
        board = new Board(9);
        gameEngine = new GameEngine(board);
        blackPlayer = new GamePlayer("Black Player", StoneColor.BLACK);
        whitePlayer = new GamePlayer("White Player", StoneColor.WHITE);
        gameEngine.setPlayers(blackPlayer, whitePlayer);
    }

    @Test
    void testShouldCaptureStoneBySurroundingItInASquare() {
        // Umieszczamy jeden biały kamień na środku
        Position whiteStonePos = new Position(1, 1);
        board.setStone(whiteStonePos, StoneColor.WHITE);

        // Otaczamy go z trzech stron czarnymi kamieniami
        board.setStone(new Position(0, 1), StoneColor.BLACK); // Góra
        board.setStone(new Position(2, 1), StoneColor.BLACK); // Dół
        board.setStone(new Position(1, 0), StoneColor.BLACK); // Lewo
        // Ostatni oddech białego kamienia jest na pozycji (1, 2)

        // Sprawdzamy stan przed ruchem
        assertEquals(StoneColor.WHITE, board.getStone(whiteStonePos));
        assertEquals(0, gameEngine.getBlackCaptures());

        // ACT - Wykonujemy ruch zbijający, zamykając kwadrat
        Position capturingMovePos = new Position(1, 2); // Prawo
        Move capturingMove = new Move(capturingMovePos, blackPlayer);
        MoveResult result = gameEngine.applyMove(capturingMove);

        // ASSERT - Sprawdzamy, czy stan po ruchu jest prawidłowy
        assertTrue(result.isOk(), "Ruch zbijający powinien być prawidłowy");
        
        // Sprawdzamy, czy biały kamień został zbity (pole jest teraz puste)
        assertEquals(StoneColor.EMPTY, board.getStone(whiteStonePos), "Kamień na (1,1) powinien zostać zbity");
        
        // Sprawdzamy, czy czarny kamień, który dokonał zbicia, stoi na swoim miejscu
        assertEquals(StoneColor.BLACK, board.getStone(capturingMovePos));
        
        // Sprawdzamy, czy licznik zbić został zaktualizowany
        assertEquals(1, gameEngine.getBlackCaptures(), "Licznik zbić czarnego gracza powinien wynosić 1");
        
        // Sprawdzamy, czy tura zmieniła się na białego gracza
        assertEquals(whitePlayer, gameEngine.getCurrentPlayer(), "Tura powinna przejść na białego gracza");
    }
     @Test
    void testShouldCaptureFourStoneGroup() {
        // ARRANGE - Ustawiamy scenariusz na planszy
        // Tworzymy grupę czterech czarnych kamieni w kwadracie 2x2
        Position blackStone1 = new Position(1, 1);
        Position blackStone2 = new Position(1, 2);
        Position blackStone3 = new Position(2, 1);
        Position blackStone4 = new Position(2, 2);
        board.setStone(blackStone1, StoneColor.BLACK);
        board.setStone(blackStone2, StoneColor.BLACK);
        board.setStone(blackStone3, StoneColor.BLACK);
        board.setStone(blackStone4, StoneColor.BLACK);

        // Otaczamy grupę białymi kamieniami, zostawiając jeden oddech
        board.setStone(new Position(0, 1), StoneColor.WHITE);
        board.setStone(new Position(0, 2), StoneColor.WHITE);
        board.setStone(new Position(3, 1), StoneColor.WHITE);
        board.setStone(new Position(3, 2), StoneColor.WHITE);
        board.setStone(new Position(1, 0), StoneColor.WHITE);
        board.setStone(new Position(2, 0), StoneColor.WHITE);
        board.setStone(new Position(1, 3), StoneColor.WHITE);
        // Ostatni oddech grupy czarnych kamieni jest na pozycji (2, 3)
        gameEngine.changePlayers();
        assertEquals(whitePlayer, gameEngine.getCurrentPlayer(), "Powinna być tura białego gracza");
        assertEquals(0, gameEngine.getWhiteCaptures());

        Position capturingMovePos = new Position(2, 3);
        Move capturingMove = new Move(capturingMovePos, whitePlayer);
        MoveResult result = gameEngine.applyMove(capturingMove);

        //Sprawdzamy, czy stan po ruchu jest prawidłowy
        assertTrue(result.isOk(), "Ruch zbijający grupę powinien być prawidłowy");
        
        // Sprawdzamy, czy wszystkie cztery kamienie zostały zbite
        assertEquals(StoneColor.EMPTY, board.getStone(blackStone1), "Kamień 1 powinien zostać zbity");
        assertEquals(StoneColor.EMPTY, board.getStone(blackStone2), "Kamień 2 powinien zostać zbity");
        assertEquals(StoneColor.EMPTY, board.getStone(blackStone3), "Kamień 3 powinien zostać zbity");
        assertEquals(StoneColor.EMPTY, board.getStone(blackStone4), "Kamień 4 powinien zostać zbity");
        
        // Sprawdzamy, czy licznik zbić został poprawnie zaktualizowany
        assertEquals(4, gameEngine.getWhiteCaptures(), "Licznik zbić białego gracza powinien wynosić 4");
        
        // Sprawdzamy, czy tura zmieniła się z powrotem na czarnego gracza
        assertEquals(blackPlayer, gameEngine.getCurrentPlayer(), "Tura powinna przejść na czarnego gracza");
    }
     @Test
    void testShouldCaptureGroupAgainstWall() {
        // Tworzymy grupę dwóch czarnych kamieni przy górnej krawędzi
        Position blackStone1 = new Position(0, 0);
        Position blackStone2 = new Position(1, 0);
        board.setStone(blackStone1, StoneColor.BLACK);
        board.setStone(blackStone2, StoneColor.BLACK);

        // Otaczamy grupę białymi kamieniami, zostawiając jeden oddech
        // Oddechy tej grupy to (0,1), (1,1), (2,0)
        board.setStone(new Position(0, 1), StoneColor.WHITE);
        board.setStone(new Position(1, 1), StoneColor.WHITE);
        // Ostatni oddech grupy czarnych kamieni jest na pozycji (2, 0)

        // Zmieniamy turę na białego gracza
        gameEngine.changePlayers();
        assertEquals(whitePlayer, gameEngine.getCurrentPlayer());

        // ACT - Wykonujemy ruch zbijający na ostatnim oddechu
        Position capturingMovePos = new Position(2, 0);
        Move capturingMove = new Move(capturingMovePos, whitePlayer);
        MoveResult result = gameEngine.applyMove(capturingMove);

        // ASSERT - Sprawdzamy, czy stan po ruchu jest prawidłowy
        assertTrue(result.isOk(), "Ruch zbijający grupę przy ścianie powinien być prawidłowy");

        // Sprawdzamy, czy oba czarne kamienie zostały zbite
        assertEquals(StoneColor.EMPTY, board.getStone(blackStone1), "Kamień 1 przy ścianie powinien zostać zbity");
        assertEquals(StoneColor.EMPTY, board.getStone(blackStone2), "Kamień 2 przy ścianie powinien zostać zbity");

        // Sprawdzamy, czy licznik zbić został poprawnie zaktualizowany
        assertEquals(2, gameEngine.getWhiteCaptures(), "Licznik zbić białego gracza powinien wynosić 2");

        // Sprawdzamy, czy tura zmieniła się z powrotem na czarnego gracza
        assertEquals(blackPlayer, gameEngine.getCurrentPlayer());
    }
}