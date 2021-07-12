package Main.Objects;

import Main.Graphics.Game;
import Main.Team;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;

abstract public class Hero extends Rectangle {
    private final int power;
    private final int speed;
    private final Team team;
    private int health;
    private int score;
    private boolean attacking;

    public Hero(int power, int health, int speed, int x, int y, String img, Team team) {
        super(75, 95);
        this.power = power;
        this.health = health;
        this.speed = speed;
        this.team = team;

        setFill(Game.loadImage(img));
        setTranslateX(x);
        setTranslateY(y);
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public int getPower() {
        return power;
    }

    public int getHealth() {
        return Math.max(0, health);
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getSpeed() {
        return speed;
    }

    public boolean isAttacking() {
        return attacking;
    }

    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
    }

    public Team getTeam() {
        return team;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void attack(Hero hero) {
        hero.setHealth(hero.getHealth() - this.getPower());
        score++;
        team.setScore(team.getScore() + 1);
    }

    public void attack(Castle castle) {
        castle.setHealth(castle.getHealth() - this.getPower());
        score++;
        team.setScore(team.getScore() + 1);
    }

    public void move(final Castle castle) {

        double distanceX = castle.getTranslateX() - this.getTranslateX();
        double distanceY = castle.getTranslateY() - this.getTranslateY();

        Platform.runLater(() -> {

            if (Math.abs(distanceX) > Math.abs(distanceY)) {
                if (distanceX > 0) {
                    setTranslateX(getTranslateX() + speed);
                } else {
                    setTranslateX(getTranslateX() - speed);
                }
            } else {
                if (distanceY > 0) {
                    setTranslateY(getTranslateY() + speed);
                } else {
                    setTranslateY(getTranslateY() - speed);
                }
            }

        });

    }

    public boolean isTarget(Node shape) {
//        double distanceX = shape.getTranslateX() - this.getTranslateX();
//        double distanceY = shape.getTranslateY() - this.getTranslateY();
//        return Math.abs(distanceX) < Game.getW() &&  Math.abs(distanceY) < Game.getH();
        return this.getBoundsInParent().intersects(shape.getBoundsInParent());
    }

    public boolean isEnemy(Hero hero) {
        return this.getTeam() != hero.getTeam();
    }

    public boolean isDead() {
        return this.health <= 0;
    }

    abstract public Hero clone();
    
}
