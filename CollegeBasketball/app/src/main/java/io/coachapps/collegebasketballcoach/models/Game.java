package io.coachapps.collegebasketballcoach.models;

public class Game {
    public String homeTeam;
    public String awayTeam;
    public Stats homeStats;
    public Stats awayStats;
    public int year;
    public Game(String homeTeam, String awayTeam, int year) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.year = year;
        this.homeStats = new Stats();
        this.awayStats = new Stats();
    }
    public Game(String homeTeam, String awayTeam, int year, Stats homeStats, Stats awayStats) {
        this.homeStats = homeStats;
        this.homeTeam = homeTeam;
        this.awayStats = awayStats;
        this.awayTeam = awayTeam;
        this.year = year;
    }
}
