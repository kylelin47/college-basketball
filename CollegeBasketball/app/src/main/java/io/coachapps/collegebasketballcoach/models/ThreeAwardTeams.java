package io.coachapps.collegebasketballcoach.models;

import java.io.Serializable;

public class ThreeAwardTeams implements Serializable {
    public AwardTeamModel firstTeam;
    public AwardTeamModel secondTeam;
    public AwardTeamModel thirdTeam;

    public ThreeAwardTeams() {
        firstTeam = new AwardTeamModel();
        secondTeam = new AwardTeamModel();
        thirdTeam = new AwardTeamModel();
    }

    public AwardTeamModel get(int index) {
        if (index == 0) return firstTeam;
        else if (index == 1) return secondTeam;
        else return thirdTeam;
    }
}
