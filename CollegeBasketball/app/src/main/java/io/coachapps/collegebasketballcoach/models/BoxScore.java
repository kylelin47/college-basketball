package io.coachapps.collegebasketballcoach.models;

public class BoxScore {
    public int playerId;
    public int year;

    public PlayerStats playerStats;

    public BoxScore(int playerId, int year) {
        this.playerId = playerId;
        this.year = year;
        this.playerStats = new PlayerStats();
    }
}
