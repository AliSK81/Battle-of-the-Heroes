package Main.Graphics;

import Main.Main;
import Main.Objects.*;
import Main.Team;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Game {

    private final Pane root = new Pane();
    private final Castle CASTLE_TWO;
    private final Castle CASTLE_ONE;
    private final List<Hero> allHeroes;
    private final Spinner<Double> speed = new Spinner<>();
    private final TableView<Hero> scores = new TableView<>();
    private final Label teamOneScore = new Label();
    private final Label teamTwoScore = new Label();
    private final int w = getScreenSize().width;
    private final int h = getScreenSize().height;
    private Node header;
    private Timeline timeline;
    private boolean pause;

    public Game() {
        CASTLE_ONE = new Castle(10, h / 2, Team.ONE);
        CASTLE_TWO = new Castle(w, h / 2, Team.TWO);
        allHeroes = new ArrayList<>();
        initNewGame();
        initGame();
    }

    public Game(Castle CASTLE_ONE, Castle CASTLE_TWO, List<Hero> allHeroes) {
        this.CASTLE_ONE = CASTLE_ONE;
        this.CASTLE_TWO = CASTLE_TWO;
        this.allHeroes = allHeroes;
        initOldGame();
        initGame();
        if (gameOver()) {
            finishGame();
        }
    }

    private void initTimeLine() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(0.07 / speed.getValue()), event -> updateGame()));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    public static ImagePattern loadImage(String name) {
        return new ImagePattern(
                new Image(Objects.requireNonNull(Main.class.getClassLoader().
                        getResourceAsStream("pics/" + name))));
    }

    public static Dimension getScreenSize() {
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        size.setSize(size.getWidth() - 140, size.getHeight());
        return size;
    }

    private Node makeFirstHeader() {

        Group group = new Group(
                new Ice(0, 0, Team.ONE),
                new Fire(75, 0, Team.ONE),
                new Wind(2 * 75, 0, Team.ONE),
                new Soil(3 * 75, 0, Team.ONE),

                new Soil(w - 2 * 75, 0, Team.TWO),
                new Wind(w - 75, 0, Team.TWO),
                new Fire(w, 0, Team.TWO),
                new Ice(w + 75, 0, Team.TWO)
        );

        group.getChildren().stream().map(shape -> (Hero) shape).forEach(ch -> {

            AtomicReference<Hero> hero = new AtomicReference<>();

            ch.setOnMousePressed(e -> {
                hero.set(ch.clone());
                root.getChildren().add(hero.get());
            });

            final AtomicBoolean canMove = new AtomicBoolean(false);

            ch.setOnMouseDragged(e -> {

                switch (hero.get().getTeam()) {
                    case ONE -> canMove.set(e.getSceneX() < w * 0.2);
                    case TWO -> canMove.set(e.getSceneX() > w * 0.8);
                }

                for (Node addedNode : root.getChildren()) {
                    if (!hero.get().equals(addedNode))
                        canMove.set(canMove.get() && !hero.get().isTarget(addedNode));
                }

                if (canMove.get()) {
                    root.getScene().setCursor(Cursor.CLOSED_HAND);
                } else {
                    root.getScene().setCursor(Cursor.CROSSHAIR);
                }

                hero.get().setTranslateX(e.getSceneX() - 40);
                hero.get().setTranslateY(e.getSceneY() - 40);
            });

            ch.setOnMouseReleased(e -> {
                root.getScene().setCursor(Cursor.DEFAULT);
                if (canMove.get()) {
                    allHeroes.add(hero.get());
                } else {
                    root.getChildren().remove(hero.get());
                }
            });

        });

        return header = group;
    }

    private Node makeSecondHeader() {

        VBox vBox = new VBox(speed, teamOneScore, teamTwoScore);
        Button pause = new Button("Pause");
        Button save = new Button("Save");
        Button menu = new Button("Menu");
        VBox buttons = new VBox(pause, save, menu);

        String style = "-fx-font-size: 20;" +
                "-fx-text-fill: white;" +
                "-fx-background-color: linear-gradient(to top, #3c1053, #ad5389);";

        for (Node node : buttons.getChildren()) {
            Button btn = ((Button) node);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setMaxHeight(Double.MAX_VALUE);
            btn.setStyle(style);
        }

        for (Label lbl : new Label[]{teamOneScore, teamTwoScore}) {
            lbl.setMaxWidth(Double.MAX_VALUE);
            lbl.setMaxHeight(Double.MAX_VALUE);
            lbl.setStyle(style);
        }

        speed.setMaxWidth(Double.MAX_VALUE);
        speed.setMaxHeight(Double.MAX_VALUE);
        vBox.setSpacing(10);

        save.setOnAction(saveEvent -> new Thread(() -> {
            try {
                saveGame();
                Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, "Game saved").show());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start());

        pause.setOnAction(e -> {
            if (!gameOver()) {
                if (this.pause) {
                    startGame();
                    pause.setText("Pause");
                } else {
                    pauseGame();
                    pause.setText("Start");
                }
            }
        });

        menu.setOnAction(e -> {
            synchronized (this) {
                this.pause = true;
                notifyAll();
            }
            Main.enterMenu(new Stage());
            this.root.getScene().getWindow().hide();
        });

        TableColumn<Hero, String> heroName = new TableColumn<>("Hero Name");
        TableColumn<Hero, String> heroTeam = new TableColumn<>("Team");
        TableColumn<Hero, Integer> heroScore = new TableColumn<>("Score");

        heroName.setCellValueFactory(new PropertyValueFactory<>("name"));
        heroTeam.setCellValueFactory(new PropertyValueFactory<>("team"));
        heroScore.setCellValueFactory(new PropertyValueFactory<>("score"));

        scores.getColumns().addAll(heroName, heroTeam, heroScore);
        scores.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        scores.getItems().addAll(allHeroes);
        updateScores();

        HBox hBox = new HBox(buttons, vBox, scores);
//        hBox.setPrefSize(800, 70);
        hBox.setMaxHeight(40);
        hBox.setSpacing(20);

        return header = hBox;
    }

    private void initOldGame() {

        root.getChildren().addAll(CASTLE_ONE, CASTLE_TWO);

        for (Hero hero : allHeroes) {
            if (hero.isDead()) {
                makeGrave(hero);
            } else {
                root.getChildren().add(hero);
            }
        }

        root.getChildren().add(makeSecondHeader());
    }

    private void initNewGame() {

        root.getChildren().addAll(CASTLE_ONE, CASTLE_TWO);

        getCastles().forEach(castle -> {

            final AtomicBoolean canMove = new AtomicBoolean(false);
            final AtomicInteger startX = new AtomicInteger((int) castle.getTranslateX());
            final AtomicInteger startY = new AtomicInteger((int) castle.getTranslateY());

            castle.setOnMouseDragged(e -> {

                switch (castle.getTeam()) {
                    case ONE -> canMove.set(e.getSceneX() < w * 0.05);
                    case TWO -> canMove.set(e.getSceneX() > w * 0.95);
                }

                for (Node addedNode : root.getChildren()) {
                    if (!castle.equals(addedNode))
                        canMove.set(canMove.get() && !castle.isTarget(addedNode));
                }

                if (canMove.get()) {
                    root.getScene().setCursor(Cursor.CLOSED_HAND);
                    startX.set((int) castle.getTranslateX());
                    startY.set((int) castle.getTranslateY());

                } else {
                    root.getScene().setCursor(Cursor.CROSSHAIR);
                }

                castle.setTranslateX(e.getSceneX() - 40);
                castle.setTranslateY(e.getSceneY() - 40);
            });

            castle.setOnMouseReleased(e -> {
                root.getScene().setCursor(Cursor.DEFAULT);
                if (!canMove.get()) {
                    castle.setTranslateX(startX.get());
                    castle.setTranslateY(startY.get());
                }
            });

        });
        Team.ONE.setScore(0);
        Team.TWO.setScore(0);

        root.getChildren().add(makeFirstHeader());
    }

    private void initGame() {

        root.setPrefSize(w, h);

        root.setBackground(Background.EMPTY);

        speed.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.25, 5, 1, 0.25));

        speed.setOnMouseClicked(e -> {
            if (!pause) {
                timeline.stop();
                initTimeLine();
                timeline.play();
            }
        });

        initTimeLine();

        Button start = new Button("Start");
        start.setTranslateX(w / 2d);
        start.setTranslateY(h / 2d);
        start.setStyle("-fx-font-size: 20; -fx-text-fill: white; -fx-background-color: red");
        root.getChildren().add(start);

        start.setOnAction(e -> {
            root.getChildren().remove(start);
            getCastles().forEach(castle -> castle.setOnMouseDragged(ignored -> {}));
            if (header instanceof Group) {
                root.getChildren().remove(header);
                root.getChildren().add(makeSecondHeader());
            }
            timeline.play();
        });
    }

    private void updateGame() {

        if (gameOver()) {
            finishGame();
        }

        getHeroes().forEach(hero -> {

            if (!hero.isAttacking())

                getHeroes().stream().filter(hero::isEnemy).forEach(enemy -> {

                    if (hero.isTarget(enemy)) {
                        hero.setAttacking(true);

                        new Thread(() -> {

                            Label heroHealth = makeHealthLabel(hero);

                            while (!hero.isDead() && !enemy.isDead() && !gameOver()) {

                                while (pause) {
                                    synchronized (this) {
                                        try {
                                            this.wait();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                Platform.runLater(() -> heroHealth.setText(String.valueOf(hero.getHealth())));
                                hero.attack(enemy);
                                sleep(600);
                            }
                            hero.setAttacking(false);

                            Platform.runLater(() -> {
                                root.getChildren().remove(heroHealth);
                                if (hero.isDead()) {
                                    Platform.runLater(() -> makeGrave(hero));
                                }
                            });

                        }).start();
                    }
                });

            final Castle enemyCastle = (hero.getTeam() == Team.ONE) ? CASTLE_TWO : CASTLE_ONE;

            if (!hero.isAttacking() && hero.isTarget(enemyCastle)) {
                hero.setAttacking(true);

                new Thread(() -> {

                    final double totalHealth = 2500;
                    ProgressBar pb = makeHealthProgressBar(enemyCastle);

                    while (!gameOver()) {

                        while (pause) {
                            synchronized (this) {
                                try {
                                    this.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        Platform.runLater(() -> pb.setProgress(enemyCastle.getHealth() / totalHealth));
                        hero.attack(enemyCastle);
                        sleep(700);
                    }
                }).start();
            }

            if (!hero.isAttacking()) {
                hero.move(enemyCastle);
            }

            updateScores();
        });
    }

    private List<Hero> getHeroes() {
        return root.getChildren().stream().filter(node -> node instanceof Hero).map(h -> (Hero) h).collect(Collectors.toList());
    }

    private List<Castle> getCastles() {
        return root.getChildren().stream().filter(node -> node instanceof Castle).map(c -> (Castle) c).collect(Collectors.toList());
    }

    private void updateScores() {
        scores.refresh();
        teamOneScore.setText("TEAM ONE SCORE : " + Team.ONE.getScore());
        teamTwoScore.setText("TEAM TWO SCORE : " + Team.TWO.getScore());
    }

    private ProgressBar makeHealthProgressBar(Shape shape) {
        ProgressBar pb = new ProgressBar();
        pb.setTranslateX(shape.getTranslateX());
        pb.setTranslateY(shape.getTranslateY() - 10);
        pb.setStyle("-fx-accent: red;");
        Platform.runLater(() -> root.getChildren().add(2, pb));
        return pb;
    }

    private Label makeHealthLabel(Shape shape) {
        Label health = new Label();
        health.setStyle("-fx-background-color : pink");
        health.setTranslateX(shape.getTranslateX());
        health.setTranslateY(shape.getTranslateY());
        Platform.runLater(() -> root.getChildren().add(health));
        return health;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep((long) (millis / speed.getValue()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean gameOver() {
        return CASTLE_ONE.isDestroyed() || CASTLE_TWO.isDestroyed();
    }

    public void makeGrave(Hero hero) {

        root.getChildren().remove(hero);

        Rectangle rect = new Rectangle(50, 59);
        rect.setTranslateX(hero.getTranslateX());
        rect.setTranslateY(hero.getTranslateY());
        rect.setFill(loadImage("dead_hero.png"));

        root.getChildren().add(2, rect);
    }

    public void finishGame() {

        Label label = new Label(" Game Over ");
        label.setStyle("-fx-text-fill: white; -fx-font-size: 50; -fx-weight: bold; -fx-background-color: red ");
        label.setTranslateX(w / 2d);
        label.setTranslateY(h / 2d);

        root.getChildren().add(label);

        if (CASTLE_ONE.isDestroyed()) {
            CASTLE_ONE.setFill(loadImage("destroyed_castle.png"));
        } else {
            CASTLE_TWO.setFill(loadImage("destroyed_castle.png"));
        }

        timeline.stop();
    }

    private void saveGame() throws IOException {

        File file = new File("data.txt");

        new FileWriter(file).close();
        FileWriter fw = new FileWriter(file, true);
        BufferedWriter bw = new BufferedWriter(fw);

        for (Castle castle : getCastles()) {
            bw.write(String.format("CASTLE,%d,%d,%s,%d\n",
                    (int) castle.getTranslateX(), (int) castle.getTranslateY(),
                    castle.getTeam(), castle.getHealth())
            );
        }

        for (Hero hero : scores.getItems()) {
            bw.write(String.format("HERO,%d,%d,%s,%s,%d,%d\n",
                    (int) hero.getTranslateX(), (int) hero.getTranslateY(),
                    hero.getName(), hero.getTeam(), hero.getHealth(), hero.getScore())
            );
        }

        for (Team team : Team.values()) {
            bw.write(String.format("TEAM-SCORE,%s,%d\n", team.name(), team.getScore()));
        }

        bw.close();
        fw.close();
    }

    synchronized public void pauseGame() {
        pause = true;
        timeline.stop();
        notifyAll();
    }

    synchronized private void startGame() {
        pause = false;
        initTimeLine();
        timeline.play();
        notifyAll();
    }

    public Pane getRoot() {
        return root;
    }

}
