package io.coachapps.collegebasketballcoach.models;

public class BoxScore {
    public int points = 0;
    public int playerId;
    public int year;
    public BoxScore(int playerId, int year) {
        this.playerId = playerId;
        this.year = year;
    }
}
