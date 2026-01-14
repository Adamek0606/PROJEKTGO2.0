package pl.pwr.gogame.client;



import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pl.pwr.gogame.client.view.GameView;

public class GoClientApp extends Application{
    
   
   @Override
public void start(Stage stage) {
    GameView view = new GameView();
    new GameController(view);

    Scene scene = new Scene(view.getRoot(), 800, 800);
    stage.setTitle("Go Game");
    stage.setScene(scene);
    stage.show();

    view.getInputField().requestFocus();
}

        
    

    public static void main(String[] args) {
        launch(args);
    }
}
