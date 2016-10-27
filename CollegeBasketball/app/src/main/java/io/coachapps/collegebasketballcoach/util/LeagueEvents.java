package io.coachapps.collegebasketballcoach.util;

import android.content.Context;

import java.util.List;

import io.coachapps.collegebasketballcoach.basketballsim.Game;
import io.coachapps.collegebasketballcoach.basketballsim.Simulator;
import io.coachapps.collegebasketballcoach.basketballsim.Team;
import io.coachapps.collegebasketballcoach.db.GameDao;
import io.coachapps.collegebasketballcoach.models.GameModel;

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

    public static boolean playGame(List<Team> teams, Simulator sim) {
        int week = determineWeek(teams);
        if (week == -1) return false;
        for (Team t : teams) {
            if (!t.gameSchedule.get(week).hasPlayed()) {
                t.gameSchedule.get(week).playGame(sim);
            }
        }
        return true;
    }

    private static int determineWeek(List<Team> teams) {
        // look at the last team to play to determine week. This accounts for the case
        // where the phone crashed mid-sim and only some games have been played
        for (Game game : teams.get(teams.size() - 1).gameSchedule) {
            if (!game.hasPlayed()) {
                return game.getWeek();
            }
        }
        return -1;
    }
}
