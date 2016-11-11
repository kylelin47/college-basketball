package io.coachapps.collegebasketballcoach.models;

public class BoxScore {
    public int playerId;
    public int year;
    public int week;
    public String teamName;

    public Stats playerStats;

    public BoxScore(int playerId, int year, int week, Stats playerStats, String teamName) {
        this.playerId = playerId;
        this.year = year;
        this.week = week;
        this.playerStats = playerStats;
        this.teamName = teamName;
    }
}
