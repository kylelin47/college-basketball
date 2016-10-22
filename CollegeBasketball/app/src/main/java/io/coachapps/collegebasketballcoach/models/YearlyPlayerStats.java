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
    public String getPGDisplay(final String abbreviation) {
        if (abbreviation.contains("%")) {
            return String.valueOf((int)getPG(abbreviation));
        }
        return String.format("%.1f", getPG(abbreviation));
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
            case "SPG":
                return getPG(playerStats.steals);
            case "BPG":
                return getPG(playerStats.blocks);
            case "FGA":
                return getPG(playerStats.fieldGoalsAttempted);
            case "FGM":
                return getPG(playerStats.fieldGoalsMade);
            case "FG%":
                if (playerStats.fieldGoalsAttempted == 0) return 0;
                return ((float)playerStats.fieldGoalsMade/playerStats.fieldGoalsAttempted *
                        100);
            case "TPG":
                return getPG(playerStats.turnovers);
            case "3PA":
                return getPG(playerStats.threePointsAttempted);
            case "3PM":
                return getPG(playerStats.threePointsMade);
            case "3P%":
                if (playerStats.threePointsAttempted == 0) return 0;
                return ((float)playerStats.threePointsMade/playerStats.threePointsAttempted *
                    100);
            case "FTA":
                return getPG(playerStats.freeThrowsAttempted);
            case "FTM":
                return getPG(playerStats.freeThrowsMade);
            case "FT%":
                if (playerStats.freeThrowsAttempted == 0) return 0;
                return ((float)playerStats.freeThrowsMade/playerStats.freeThrowsAttempted *
                        100);
            default:
                return 0;
        }
    }
    private float getPG(float total) {
        return gamesPlayed == 0 ? 0 : total/gamesPlayed;
    }
}
