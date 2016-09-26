package io.coachapps.collegebasketballcoach.models;

public class YearlyPlayerStats {
    public int playerId;
    public int year;

    public int gamesPlayed;

    public Stats playerStats;

    public YearlyPlayerStats(int playerId){
        this.playerId = playerId;
        this.gamesPlayed = 0;

        this.playerStats = new Stats();
    }

    public YearlyPlayerStats(BoxScore boxScore) {
        this.year = boxScore.year;
        this.gamesPlayed = 1;
        this.playerId = boxScore.playerId;
        this.playerStats = new Stats(boxScore.playerStats);
    }

    public float getPPG() {
        if (gamesPlayed == 0) {
            return 0;
        } else {
            return (float)playerStats.points/gamesPlayed;
        }
    }
}
