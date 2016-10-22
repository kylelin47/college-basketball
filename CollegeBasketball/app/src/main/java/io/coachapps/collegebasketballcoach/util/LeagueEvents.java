package io.coachapps.collegebasketballcoach.util;

import java.util.List;

import io.coachapps.collegebasketballcoach.basketballsim.Game;
import io.coachapps.collegebasketballcoach.basketballsim.Simulator;
import io.coachapps.collegebasketballcoach.basketballsim.Team;

/**
 * League Events utility class. Will be used to perform various league activities,
 * such as scheduling games, picking All-Americans, etc.
 * Created by Achi Jones on 10/22/2016.
 */

public class LeagueEvents {

    public static void scheduleSeason(List<Team> teams) {
        int robinRounds = teams.size() - 1;
        int halfRobin = teams.size()/2;
        for (int r = 0; r < robinRounds; ++r) {
            for (int g = 0; g < halfRobin; ++g) {
                Team a = teams.get((r + g) % robinRounds);
                Team b;
                if ( g == 0 ) {
                    b = teams.get(robinRounds);
                } else {
                    b = teams.get((robinRounds - g + r) % robinRounds);
                }

                Game gm = new Game(a, b, 0);

                a.gameSchedule.add(gm);
                b.gameSchedule.add(gm);
            }
        }
    }

    public static void playGame(int game, List<Team> teams, Simulator sim) {
        for (Team t : teams) {
            if (!t.gameSchedule.get(game).hasPlayed()) {
                t.gameSchedule.get(game).playGame(sim);
            }
        }
    }

}
