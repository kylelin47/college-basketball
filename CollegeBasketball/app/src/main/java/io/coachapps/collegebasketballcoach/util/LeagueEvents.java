package io.coachapps.collegebasketballcoach.util;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import io.coachapps.collegebasketballcoach.basketballsim.Game;
import io.coachapps.collegebasketballcoach.basketballsim.Player;
import io.coachapps.collegebasketballcoach.basketballsim.Simulator;
import io.coachapps.collegebasketballcoach.basketballsim.Team;
import io.coachapps.collegebasketballcoach.db.BoxScoreDao;
import io.coachapps.collegebasketballcoach.db.GameDao;
import io.coachapps.collegebasketballcoach.models.BoxScore;
import io.coachapps.collegebasketballcoach.models.GameModel;
import io.coachapps.collegebasketballcoach.models.Stats;

/**
 * League Events utility class. Will be used to perform various league activities,
 * such as scheduling games, picking All-Americans, etc.
 * Created by Achi Jones on 10/22/2016.
 */

public class LeagueEvents {
    private static int determineYear() {
        return 2016;
    }
    public static void scheduleSeason(List<Team> teams, Context context) {
        int robinRounds = teams.size() - 1;
        int halfRobin = teams.size()/2;
        int year = determineYear();
        int week = 0;
        GameDao gameDao = new GameDao(context);
        for (int r = 0; r < robinRounds; ++r) {
            for (int g = 0; g < halfRobin; ++g) {
                Team home = teams.get((r + g) % robinRounds);
                Team away;
                if ( g == 0 ) {
                    away = teams.get(robinRounds);
                } else {
                    away = teams.get((robinRounds - g + r) % robinRounds);
                }
                // if this is slow, make it fetch all games played this year for these teams
                // in a single transaction block
                GameModel gameModel = gameDao.getGame(year, week, home.getName(), away.getName());
                Game gameToSchedule;
                if (gameModel == null) {
                    gameToSchedule = new Game(home, away, year, week);
                } else {
                    gameToSchedule = new Game(home, away, gameModel);
                }
                home.gameSchedule.add(gameToSchedule);
                away.gameSchedule.add(gameToSchedule);
            }
            week++;
        }
    }

    public static boolean playGame(List<Team> teams, Simulator sim,
                                   boolean simUserGame, String userTeamName) {
        int week = determineWeek(teams);
        if (week == Integer.MAX_VALUE) return false;
        for (Team t : teams) {
            Game gm = t.gameSchedule.get(week);
            if (!gm.hasPlayed() && (simUserGame ||
                    (!gm.getAway().getName().equals(userTeamName) &&
                     !gm.getHome().getName().equals(userTeamName)))) {
                gm.playGame(sim);
            }
        }
        return true;
    }

    public static GameModel saveGameResult(Context context, Team home, Team away, int year, int week) {
        BoxScoreDao bsd = new BoxScoreDao(context);
        List<BoxScore> boxScores = new ArrayList<>();
        for (int p = 0; p < 10; ++p) {
            boxScores.add(home.players.get(p).getGameBoxScore(year, week, home.getName()));
            boxScores.add(away.players.get(p).getGameBoxScore(year, week, away.getName()));
        }
        bsd.save(boxScores);

        home.resetLineup();
        away.resetLineup();

        Stats homeStats = new Stats();
        for (Player player : home.players) {
            homeStats.add(player.gmStats);
        }
        Stats awayStats = new Stats();
        for (Player player : away.players) {
            awayStats.add(player.gmStats);
        }
        GameDao gameDao = new GameDao(context);
        GameModel gameResult = new GameModel(home.name, away.name, year, week, homeStats,
                awayStats);
        gameDao.save(gameResult);
        return gameResult;
    }

    public static int determineWeek(List<Team> teams) {
        // look at the last team to play to determine week. This accounts for the case
        // where the phone crashed mid-sim and only some games have been played
        // However this would mess up play-by-play with the last team, so I'm changing it for now
        int minWeek = Integer.MAX_VALUE;
        for (Team t : teams) {
            for (Game game : t.gameSchedule) {
                if (!game.hasPlayed() && game.getWeek() < minWeek) {
                    minWeek = game.getWeek();
                }
            }
        }

        return minWeek;
    }
}
