package Main.Graphics;

import Main.Main;
import Main.Objects.*;
import Main.Team;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Menu {

    private final VBox root = new VBox();

    public Menu() {

        Label title = new Label("Game Menu");
        Button start = new Button("Start New Game");
        Button load = new Button("Play Last Game");
        Button exit = new Button("Exit Game");

        title.setAlignment(Pos.CENTER);

        root.getChildren().addAll(title, start, load, exit);

        for (Node node : root.getChildren()) {
            ((Control) node).setPrefSize(500, 100);
            node.setStyle("-fx-background-color: linear-gradient(to top, #001510, #dc2430);" +
                    "-fx-text-fill: white; -fx-font-size: 30;");
        }
        title.setStyle("-fx-background-color: linear-gradient(to top, #001510, #dc2430);" +
                "-fx-text-fill: yellow; -fx-font-size: 40; -fx-weight: bold");

        start.setOnAction(e -> enterGame(new Game()));

        load.setOnAction(e -> {

            try {
                enterGame(loadGame());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        exit.setOnAction(e -> System.exit(0));
    }

    private static ArrayList<String[]> readFile(File file) throws IOException {

        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        ArrayList<String[]> data = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            data.add(line.split(","));
        }
        br.close();
        fr.close();
        return data;
    }

    private void enterGame(Game game) {

        Stage stage = new Stage();
        stage.setOnCloseRequest(e -> {
            game.pauseGame();
            Main.enterMenu(new Stage());
        });

        stage.setMaximized(true);

        Scene scene = new Scene(game.getRoot());
//        scene.setFill(Game.loadImage("background_game.png"));
//        game.getRoot().getChildren().add(0, new ImageView(new Image("pics/background_game.png")));
        game.getRoot().setStyle("-fx-background-color: linear-gradient(to top, #b20a2c,  #fff5db 90%)");

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F11) {
                stage.setFullScreen(true);
            }
        });

        stage.setScene(scene);
        this.root.getScene().getWindow().hide();
        stage.show();
    }

    private Game loadGame() throws IOException {

        File data = new File("data.txt");
        if (!data.isFile()) {
            data.createNewFile();
        }

        ArrayList<Hero> heroes = new ArrayList<>();
        Castle CASTLE_ONE = null;
        Castle CASTLE_TWO = null;

        for (String[] L : readFile(data)) {

            switch (L[0]) {

                case "CASTLE" -> {
                    Castle castle = new Castle(toInt(L[1]), toInt(L[2]), Team.valueOf(L[3]));
                    castle.setHealth(toInt(L[4]));

                    switch (castle.getTeam()) {
                        case ONE -> CASTLE_ONE = castle;
                        case TWO -> CASTLE_TWO = castle;
                    }
                }

                case "HERO" -> {
                    int x = toInt(L[1]);
                    int y = toInt(L[2]);
                    Team team = Team.valueOf(L[4]);

                    Hero hero = null;
                    switch (L[3]) {
                        case "Ice" -> hero = new Ice(x, y, team);
                        case "Soil" -> hero = new Soil(x, y, team);
                        case "Wind" -> hero = new Wind(x, y, team);
                        case "Fire" -> hero = new Fire(x, y, team);
                    }
                    assert hero != null;
                    hero.setHealth(toInt(L[5]));
                    hero.setScore(toInt(L[6]));

                    heroes.add(hero);
                }

                case "TEAM-SCORE" -> {
                    switch (L[1]) {
                        case "ONE" -> Team.ONE.setScore(toInt(L[2]));
                        case "TWO" -> Team.TWO.setScore(toInt(L[2]));
                    }
                }
            }
        }

        return new Game(CASTLE_ONE, CASTLE_TWO, heroes);
    }

    private int toInt(String v) {
        return Integer.parseInt(v);
    }

    public VBox getRoot() {
        return root;
    }

}
