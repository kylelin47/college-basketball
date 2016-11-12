package io.coachapps.collegebasketballcoach.basketballsim;

import io.coachapps.collegebasketballcoach.models.FullGameResults;
import io.coachapps.collegebasketballcoach.models.GameModel;

/**
 * Game class. Game objects are in each team's game schedule list.
 * Has references to each team
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
    public boolean tournamentGame;

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

    public void schedule() {
        getHome().gameSchedule.add(this);
        getAway().gameSchedule.add(this);
    }
    /**
     * @return Home team name,Away team name,homeScore,awayScore,year,week,beenPlayed,
     * tournamentGame,homeSeed,awaySeed
     */
    @Override
    public String toString() {
        return home.getName() + "," + away.getName() + "," + homeScore + "," + awayScore + "," +
                year + "," + week + "," + beenPlayed + "," + tournamentGame + "," + home.seed +
                "," + away.seed;
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
        if (!beenPlayed) return null;
        if (homeScore > awayScore) return home;
        else return away;
    }

    public Team getLoser() {
        if (!beenPlayed) return null;
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

    public FullGameResults playGame(Simulator sim) {
        FullGameResults result = sim.playGame(home, away, year, week);
        homeScore = result.game.homeStats.points;
        awayScore = result.game.awayStats.points;
        beenPlayed = true;
        return result;
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
