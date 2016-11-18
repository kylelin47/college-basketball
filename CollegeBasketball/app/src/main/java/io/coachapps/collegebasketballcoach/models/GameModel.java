package io.coachapps.collegebasketballcoach.models;

public class GameModel {
    public String homeTeam;
    public String awayTeam;
    public TeamStats homeStats;
    public TeamStats awayStats;
    public int year;
    public int week;
    public GameModel(String homeTeam, String awayTeam, int year, int week,
                     TeamStats homeStats, TeamStats awayStats) {
        this.homeStats = homeStats;
        this.homeTeam = homeTeam;
        this.awayStats = awayStats;
        this.awayTeam = awayTeam;
        this.year = year;
        this.week = week;
    }
}
