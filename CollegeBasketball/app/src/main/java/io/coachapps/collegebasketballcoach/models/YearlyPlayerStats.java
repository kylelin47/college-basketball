package io.coachapps.collegebasketballcoach.models;

public class YearlyPlayerStats {
    public int playerId;
    public int year;

    public int gamesPlayed;

    public PlayerStats playerStats;

    public YearlyPlayerStats(int playerId){
        this.playerId = playerId;
        this.gamesPlayed = 0;

        this.playerStats = new PlayerStats();
    }

    public YearlyPlayerStats(BoxScore boxScore) {
        this.year = boxScore.year;
        this.gamesPlayed = 1;
        this.playerId = boxScore.playerId;
        this.playerStats = new PlayerStats(boxScore.playerStats);
    }

    public float getPPG() {
        if (gamesPlayed == 0) {
            return 0;
        } else {
            return (float)playerStats.points/gamesPlayed;
        }
    }
}
