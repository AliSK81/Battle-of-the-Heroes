package Main;

import Main.Graphics.Menu;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    public static void main(String[] args) {

        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        enterMenu(stage);
    }

    public static void enterMenu(Stage stage) {
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(new Scene(new Menu().getRoot()));
        stage.show();
    }
}
