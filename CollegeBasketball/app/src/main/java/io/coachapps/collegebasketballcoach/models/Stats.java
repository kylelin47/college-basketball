package io.coachapps.collegebasketballcoach.models;

import java.io.Serializable;

public class Stats implements Serializable {
    public int points = 0;
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
    public int secondsPlayed = 0;

    public Stats() {}

    public Stats(Stats stats) {
        this.points = stats.points;
        this.offensiveRebounds = stats.offensiveRebounds;
        this.defensiveRebounds = stats.defensiveRebounds;
        this.assists = stats.assists;
        this.steals = stats.steals;
        this.blocks = stats.blocks;
        this.turnovers = stats.turnovers;
        this.fouls = stats.fouls;
        this.fieldGoalsAttempted = stats.fieldGoalsAttempted;
        this.fieldGoalsMade = stats.fieldGoalsMade;
        this.threePointsAttempted = stats.threePointsAttempted;
        this.threePointsMade = stats.threePointsMade;
        this.freeThrowsAttempted = stats.freeThrowsAttempted;
        this.freeThrowsMade = stats.freeThrowsMade;
        this.secondsPlayed = stats.secondsPlayed;
    }

    public void add(Stats stats) {
        this.points += stats.points;
        this.offensiveRebounds += stats.offensiveRebounds;
        this.defensiveRebounds += stats.defensiveRebounds;
        this.assists += stats.assists;
        this.steals += stats.steals;
        this.blocks += stats.blocks;
        this.turnovers += stats.turnovers;
        this.fouls += stats.fouls;
        this.fieldGoalsAttempted += stats.fieldGoalsAttempted;
        this.fieldGoalsMade += stats.fieldGoalsMade;
        this.threePointsAttempted += stats.fieldGoalsAttempted;
        this.threePointsMade += stats.fieldGoalsMade;
        this.freeThrowsAttempted += stats.freeThrowsAttempted;
        this.freeThrowsMade += stats.freeThrowsMade;
        this.secondsPlayed += stats.secondsPlayed;
    }
}
