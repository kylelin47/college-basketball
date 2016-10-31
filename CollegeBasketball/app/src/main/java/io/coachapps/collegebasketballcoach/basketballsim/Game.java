package io.coachapps.collegebasketballcoach.basketballsim;

import io.coachapps.collegebasketballcoach.models.GameModel;

/**
 * Game class. Game objects are in each team's game schedule list.
 * Has references to each team, as well as its gameID as it is in the db.
 * Created by Achi Jones on 10/22/2016.
 */

public class Game {

    private Team home;
    private Team away;
    private int year;
    private int week;

    private int homeScore;
    private int awayScore;
    private boolean beenPlayed;

    public Game(Team home, Team away, int year, int week) {
        this.home = home;
        this.away = away;
        this.year = year;
        this.week = week;

        homeScore = 0;
        awayScore = 0;
        beenPlayed = false;
    }

    public Game(Team home, Team away, GameModel gameModel) {
        this.home = home;
        this.away = away;
        this.year = gameModel.year;
        this.week = gameModel.week;
        this.homeScore = gameModel.homeStats.points;
        this.awayScore = gameModel.awayStats.points;
        this.beenPlayed = true;
    }

    public Team getHome() {
        return home;
    }

    public Team getAway() {
        return away;
    }

    public int getHomeScore() {
        return homeScore;
    }

    public int getAwayScore() {
        return awayScore;
    }

    public int getYear() {
        return year;
    }
    public int getWeek() { return week; }

    public boolean hasPlayed() {
        return beenPlayed;
    }

    public Team getWinner() {
        if (homeScore > awayScore) return home;
        else return away;
    }

    public Team getLoser() {
        if (homeScore > awayScore) return away;
        else return home;
    }

    public String getWinnerString() {
        if (homeScore > awayScore) {
            return "W " + homeScore + "-" + awayScore;
        } else {
            return "W " + awayScore + "-" + homeScore;
        }
    }

    public String getLoserString() {
        if (homeScore > awayScore) {
            return "L " + awayScore + "-" + homeScore;
        } else {
            return "L " + homeScore + "-" + awayScore;
        }
    }

    public void playGame(Simulator sim) {
        GameModel result = sim.playGame(home, away, year, week);
        homeScore = result.homeStats.points;
        awayScore = result.awayStats.points;
        beenPlayed = true;
    }

    public void setGameModel(GameModel result) {
        homeScore = result.homeStats.points;
        awayScore = result.awayStats.points;
        beenPlayed = true;
    }

    public void setBeenPlayed(boolean p) {
        beenPlayed = p;
    }
}
