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
    private int gameID;

    private int homeScore;
    private int awayScore;
    private boolean beenPlayed;

    public Game(Team home, Team away, int gameID) {
        this.home = home;
        this.away = away;
        this.gameID = gameID;

        homeScore = 0;
        awayScore = 0;
        beenPlayed = false;
    }

    public Team getHome() {
        return home;
    }

    public Team getAway() {
        return away;
    }

    public int getGameID() {
        return gameID;
    }

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

    public void playGame(Simulator sim, int year, int week) {
        GameModel result = sim.playGame(home, away, year, week);
        homeScore = result.homeStats.points;
        awayScore = result.awayStats.points;
        beenPlayed = true;
    }
}
