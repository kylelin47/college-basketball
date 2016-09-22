package io.coachapps.collegebasketballcoach.models;

public class YearlyPlayerStats {
    public int gamesPlayed;
    public int year;
    public int playerId;
    public int points;

    public YearlyPlayerStats(){};

    public YearlyPlayerStats(BoxScore boxScore) {
        this.year = boxScore.year;
        this.playerId = boxScore.playerId;
        this.points = boxScore.points;
        this.gamesPlayed = 1;
    }

    public YearlyPlayerStats(int gamesPlayed, int year, int playerId, int points) {
        this.gamesPlayed = gamesPlayed;
        this.year = year;
        this.playerId = playerId;
        this.points = points;
    }

    public float getPPG() {
        if (gamesPlayed == 0) {
            return 0;
        } else {
            return (float)points/gamesPlayed;
        }
    }
}
