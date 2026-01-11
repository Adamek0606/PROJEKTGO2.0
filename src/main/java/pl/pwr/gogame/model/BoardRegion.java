package pl.pwr.gogame.model;

import java.util.Set;

//ta klasa jest nam potrzebna do sprawdzania typów terytorium. 
public class BoardRegion {
    Set<Position> points;
    //tutaj zbieramy kolory kamieni otaczających terytorium. Jeśli całe terytorium jest otoczone
    //jednym kolorem, należy ono do gracza tego koloru
    Set<StoneColor> borderingColors;
}
