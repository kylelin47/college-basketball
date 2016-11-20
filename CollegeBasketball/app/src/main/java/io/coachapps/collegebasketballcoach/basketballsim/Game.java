package io.coachapps.collegebasketballcoach.basketballsim;

import java.util.List;

import io.coachapps.collegebasketballcoach.models.FullGameResults;
import io.coachapps.collegebasketballcoach.models.GameModel;

/**
 * Game class. Game objects are in each team's game schedule list.
 * Has references to each team
 * Created by Achi Jones on 10/22/2016.
 */

public class Game {
    public enum GameType {
        REGULAR_SEASON("Reg. Season"),
        OUT_OF_CONFERENCE("Out Conference"),
        TOURNAMENT_GAME("Tournament"),
        MARCH_MADNESS("March Madness");
        private String value;
        GameType(String value) {
            this.value = value;
        }
        @Override
        public String toString() {
            return value;
        }
        public boolean isTournament() {
            return (this == TOURNAMENT_GAME || this == MARCH_MADNESS);
        }
    }

    private Team home;
    private Team away;
    private int year;

    private int homeScore;
    private int awayScore;
    private boolean beenPlayed;
    public GameType gameType;

    public Game(Team home, Team away, int year) {
        this.home = home;
        this.away = away;
        this.year = year;

        homeScore = 0;
        awayScore = 0;
        beenPlayed = false;
    }

    public Game(Team home, Team away, GameModel gameModel) {
        this.home = home;
        this.away = away;
        apply(gameModel);

    }

    public void apply(GameModel gameModel) {
        if (gameModel == null) return;
        this.year = gameModel.year;
        this.homeScore = gameModel.homeStats.stats.points;
        this.awayScore = gameModel.awayStats.stats.points;
        this.beenPlayed = true;
    }

    public void schedule(int week, GameType gameType) {
        this.gameType = gameType;
        getHome().gameSchedule.add(week, this);
        getAway().gameSchedule.add(week, this);
    }

    public void reschedule() {
        int homeWeek = home.gameSchedule.indexOf(this);
        int awayWeek = away.gameSchedule.indexOf(this);
        if (homeWeek < awayWeek) {
            swap(away.gameSchedule, homeWeek, awayWeek);
        } else if (awayWeek < homeWeek) {
            swap(home.gameSchedule, homeWeek, awayWeek);
        }
    }

    private void swap(List<Game> schedule, int indexOne, int indexTwo) {
        Game temp = schedule.get(indexOne);
        schedule.set(indexOne, schedule.get(indexTwo));
        schedule.set(indexTwo, temp);
    }

    /**
     * @return Home team name,Away team name,homeScore,awayScore,year,week,beenPlayed,
     * gameType,homeConfSeed,awayConfSeed,homeMarchSeed,awayMarchSeed,homeConference
     */
    @Override
    public String toString() {
        return home.getName() + "," + away.getName() + "," + homeScore + "," + awayScore + "," +
                year + "," + getWeek() + "," + beenPlayed + "," + gameType.toString() + "," +
                home.conferenceSeed + "," + away.conferenceSeed + "," + home.madnessSeed + "," +
                away.madnessSeed + "," + home.conference;
    }

    public boolean hasTeam(String teamName) {
        return (home.getName().equals(teamName) || away.getName().equals(teamName));
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
    public int getWeek() { return home.gameSchedule.indexOf(this); }

    public boolean hasPlayed() {
        return beenPlayed;
    }

    public Team getWinner() {
        if (!beenPlayed) return null;
        if (homeScore > awayScore) return home;
        else return away;
    }

    String getWinnerString() {
        if (homeScore > awayScore) {
            return "W " + homeScore + "-" + awayScore;
        } else {
            return "W " + awayScore + "-" + homeScore;
        }
    }

    String getLoserString() {
        if (homeScore > awayScore) {
            return "L " + awayScore + "-" + homeScore;
        } else {
            return "L " + homeScore + "-" + awayScore;
        }
    }

    public FullGameResults playGame(Simulator sim) {
        FullGameResults result = sim.playGame(home, away, year, getWeek());
        homeScore = result.game.homeStats.stats.points;
        awayScore = result.game.awayStats.stats.points;
        beenPlayed = true;
        return result;
    }

    void setGameModel(GameModel result) {
        homeScore = result.homeStats.stats.points;
        awayScore = result.awayStats.stats.points;
        beenPlayed = true;
    }
}
