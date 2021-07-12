package Main.Objects;

import Main.Team;

public class Soil extends Hero {
    
    public Soil(int x, int y, Team team) {
        super(30, 450, 2, x, y, (team == Team.ONE) ? "soil_one.png" : "soil_two.png", team);
    }

    @Override
    public Hero clone() {
        return new Soil((int) getTranslateX(), (int) getTranslateY(), getTeam());
    }
    
}
