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
    public float getPG(final String abbreviation) {
        switch(abbreviation) {
            case "APG":
                return getPG(playerStats.assists);
            case "PPG":
                return getPG(playerStats.points);
            case "RPG":
                return getPG(playerStats.defensiveRebounds + playerStats.offensiveRebounds);
            case "MPG":
                return getPG((float)playerStats.secondsPlayed/60);
            default:
                return 0;
        }
    }
    private float getPG(float total) {
        return gamesPlayed == 0 ? 0 : total/gamesPlayed;
    }
}
