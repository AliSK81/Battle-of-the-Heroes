package Main.Objects;

import Main.Team;

public class Wind extends Hero {

    public Wind(int x, int y, Team team) {
        super(25, 350, 3, x, y, (team == Team.ONE) ? "wind_one.png" : "wind_two.png", team);
    }

    @Override
    public Hero clone() {
        return new Wind((int) getTranslateX(), (int) getTranslateY(), getTeam());
    }

}
