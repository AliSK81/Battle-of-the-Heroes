package Main.Objects;

import Main.Team;

public class Ice extends Hero {

    public Ice(int x, int y, Team team) {
        super(15, 600, 1, x, y, (team == Team.ONE) ? "ice_one.png" : "ice_two.png", team);
    }

    @Override
    public Hero clone() {
        return new Ice((int) getTranslateX(), (int) getTranslateY(), getTeam());
    }

}
