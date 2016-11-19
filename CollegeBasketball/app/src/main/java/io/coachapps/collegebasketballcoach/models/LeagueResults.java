package io.coachapps.collegebasketballcoach.models;

public class LeagueResults {
    public int year;
    public String championTeamName;
    public int dpoyId;
    public int mvpId;
    public ThreeAwardTeams allAmericans;
    public ThreeAwardTeams allCowboy;
    public ThreeAwardTeams allLakes;
    public ThreeAwardTeams allMountains;
    public ThreeAwardTeams allNorth;
    public ThreeAwardTeams allPacific;
    public ThreeAwardTeams allSouth;

    public ThreeAwardTeams getTeam(int index) {
        switch (index) {
            case 0: return allAmericans;
            case 1: return allCowboy;
            case 2: return allLakes;
            case 3: return allMountains;
            case 4: return allNorth;
            case 5: return allPacific;
            case 6: return allSouth;
            default: return allAmericans;
        }
    }
}
