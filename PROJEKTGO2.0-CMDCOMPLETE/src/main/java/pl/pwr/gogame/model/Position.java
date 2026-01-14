package pl.pwr.gogame.model;
//wzorzec: Value Object
//rekord pozwala nam na łatwe tworzenie danych które nie będą się zmieniać. Do elementu rekordu, np. x, dostajemy się poprzez position.x()
public record Position(int col, int row) {
    
}
