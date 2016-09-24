package io.coachapps.collegebasketballcoach.models;

public class YearlyTeamStats {
    public String team;
    public int year;
    public int wins;
    public int losses;
    public YearlyTeamStats(String team){
        this.team = team;
        this.wins = 0;
        this.losses = 0;
    };
    public YearlyTeamStats(String team, int year, int wins, int losses) {
        this.team = team;
        this.year = year;
        this.wins = wins;
        this.losses = losses;
    }
}
