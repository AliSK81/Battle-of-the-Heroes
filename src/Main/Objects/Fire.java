package Main.Objects;

import Main.Team;

public class Fire extends Hero {
    
    public Fire(int x, int y, Team team) {
        super(40, 400, 2, x, y, (team == Team.ONE) ? "fire_one.png" : "fire_two.png", team);
    }

    @Override
    public Hero clone() {
        return new Fire((int) getTranslateX(), (int) getTranslateY(), getTeam());
    }

}
