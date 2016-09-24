package io.coachapps.collegebasketballcoach.models;

public class PlayerStats {
    public int points = 0;
    public int minutes = 0;
    public int offensiveRebounds = 0;
    public int defensiveRebounds = 0;
    public int assists = 0;
    public int steals = 0;
    public int blocks = 0;
    public int turnovers = 0;
    public int fouls = 0;
    public int fieldGoalsMade = 0;
    public int fieldGoalsAttempted = 0;
    public int threePointsAttempted = 0;
    public int threePointsMade = 0;
    public int freeThrowsAttempted = 0;
    public int freeThrowsMade = 0;

    public PlayerStats() {}

    public PlayerStats(PlayerStats playerStats) {
        this.points = playerStats.points;
        this.minutes = playerStats.minutes;
        this.offensiveRebounds = playerStats.offensiveRebounds;
        this.defensiveRebounds = playerStats.defensiveRebounds;
        this.assists = playerStats.assists;
        this.steals = playerStats.steals;
        this.blocks = playerStats.blocks;
        this.turnovers = playerStats.turnovers;
        this.fouls = playerStats.fouls;
        this.fieldGoalsAttempted = playerStats.fieldGoalsAttempted;
        this.fieldGoalsMade = playerStats.fieldGoalsMade;
        this.threePointsAttempted = playerStats.fieldGoalsAttempted;
        this.threePointsMade = playerStats.fieldGoalsMade;
        this.freeThrowsAttempted = playerStats.freeThrowsAttempted;
        this.freeThrowsMade = playerStats.freeThrowsMade;
    }
}
