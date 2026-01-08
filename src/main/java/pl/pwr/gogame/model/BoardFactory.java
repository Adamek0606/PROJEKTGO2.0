package pl.pwr.gogame.model;
// Fabryka tworząca plansze do gry Go
public class BoardFactory {
    
    //Tworzymy tutaj board dla reszty aplikacji
    public static Board createBoard(int size) {
        //plansza może mieć tylko określone rozmiary
        if (size != 9 && size != 13 && size != 19) {
            throw new IllegalArgumentException("Dozwolone rozmiary planszy to: 9, 13, 19");
        }
        return new Board(size);
    }
}