package Main.Objects;

import Main.Graphics.Game;
import Main.Team;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;

public class Castle extends Rectangle {
    private final Team team;
    private int health;

    public Castle(int x, int y, Team team) {
        super(120, 105);
        this.health = 2500;
        this.team = team;

        setFill(Game.loadImage("castle_one.png"));
        setTranslateX(x);
        setTranslateY(y);
    }

    public int getHealth() {
        return Math.max(0, health);
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public Team getTeam() {
        return team;
    }

    public boolean isDestroyed() {
        return this.health <= 0;
    }

    public boolean isTarget(Node shape) {
        return this.getBoundsInParent().intersects(shape.getBoundsInParent());
    }
}
