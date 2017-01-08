package io.coachapps.collegebasketballcoach.models;

public class YearlyTeamWL {

    public int wins;
    public int losses;
    public int year;
    public String team;

    public YearlyTeamWL() {
        wins = 0;
        losses = 0;
        year = 0;
        team = "";
    }

    public YearlyTeamWL(String team, int wins, int losses) {
        this.wins = wins;
        this.losses = losses;
        year = 0;
        this.team = team;
    }

    public double getWinPercentage() {
        return (double)(wins)/(wins + losses);
    }
}
