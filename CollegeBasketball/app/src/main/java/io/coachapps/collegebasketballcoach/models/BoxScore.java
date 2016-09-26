package io.coachapps.collegebasketballcoach.models;

public class BoxScore {
    public int playerId;
    public int year;

    public Stats playerStats;

    public BoxScore(int playerId, int year) {
        this.playerId = playerId;
        this.year = year;
        this.playerStats = new Stats();
    }

    public BoxScore(int playerId, int year, Stats playerStats) {
        this.playerId = playerId;
        this.year = year;
        this.playerStats = playerStats;
    }
}
