package io.coachapps.collegebasketballcoach.models;

public class GameModel {
    public String homeTeam;
    public String awayTeam;
    public Stats homeStats;
    public Stats awayStats;
    public int year;
    public int week;
    public GameModel(String homeTeam, String awayTeam, int year, int week, Stats homeStats, Stats
            awayStats) {
        this.homeStats = homeStats;
        this.homeTeam = homeTeam;
        this.awayStats = awayStats;
        this.awayTeam = awayTeam;
        this.year = year;
        this.week = week;
    }
}
