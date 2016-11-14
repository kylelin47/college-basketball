package io.coachapps.collegebasketballcoach;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.coachapps.collegebasketballcoach.basketballsim.Player;
import io.coachapps.collegebasketballcoach.basketballsim.PlayerGen;
import io.coachapps.collegebasketballcoach.basketballsim.Team;
import io.coachapps.collegebasketballcoach.db.BoxScoreDao;
import io.coachapps.collegebasketballcoach.db.DbHelper;
import io.coachapps.collegebasketballcoach.db.Schemas;
import io.coachapps.collegebasketballcoach.db.TeamDao;
import io.coachapps.collegebasketballcoach.db.YearlyPlayerStatsDao;
import io.coachapps.collegebasketballcoach.models.BoxScore;
import io.coachapps.collegebasketballcoach.models.Stats;
import io.coachapps.collegebasketballcoach.models.YearlyPlayerStats;
import io.coachapps.collegebasketballcoach.util.SerializationUtil;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class ApplicationTest {
    private Context context;
    @Before
    public void _init() {
        context = InstrumentationRegistry.getTargetContext();
        SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase();
        DbHelper.getInstance(context).resetDb(db);
        db.close();
    }
    @Test
    public void canSaveBoxScores() {
        BoxScoreDao boxScoreDao = new BoxScoreDao(context);
        BoxScore boxScore = new BoxScore(0, 2000, 0, new Stats(), "a");
        boxScore.playerStats.points = 20;
        BoxScore boxScore2 = new BoxScore(0, 2000, 1, new Stats(), "a");
        boxScore2.playerStats.points = 5;

        boxScoreDao.save(Arrays.asList(boxScore, boxScore2, new BoxScore(0, 2001, 0, new Stats(),
                "a"),
                new BoxScore(0, 2002, 0, new Stats(), "a")));
        SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase();
        String[] projection = {
                Schemas.BoxScoreEntry.STATS
        };
        List<Integer> points = new ArrayList<>();
        try (Cursor c = db.query(Schemas.BoxScoreEntry.TABLE_NAME, projection, null, null, null,
                null, null, null)) {
            while (c.moveToNext()) {
                Stats stats = (Stats) SerializationUtil.deserialize(c.getBlob(c
                        .getColumnIndexOrThrow(Schemas.BoxScoreEntry.STATS)));
                points.add(stats.points);
            }
        }
        List<Integer> yearlyPoints = new ArrayList<>();
        try (Cursor c = db.query(Schemas.YearlyPlayerStatsEntry.TABLE_NAME, null, null, null, null,
                null, null, null)) {
            while (c.moveToNext()) {
                yearlyPoints.add(c.getInt(c.getColumnIndexOrThrow(Schemas
                        .YearlyPlayerStatsEntry.POINTS)));
            }
        }
        db.close();
        assertThat(yearlyPoints.get(0), is(25));
        assertThat(yearlyPoints.size(), is(3));
        assertThat(points.size(), is(4));
        assertThat(points.get(0), is(20));
    }

    @Test
    public void canRetrieveYearlyPlayerStats() {
        BoxScoreDao boxScoreDao = new BoxScoreDao(context);
        BoxScore boxScore = new BoxScore(0, 2000, 0, new Stats(), "a");
        boxScore.playerStats.assists = 20;
        BoxScore boxScore2 = new BoxScore(0, 2000, 1, new Stats(), "a");
        boxScore2.playerStats.assists = 5;

        boxScoreDao.save(Arrays.asList(boxScore, boxScore2, new BoxScore(0, 2001, 0, new Stats(),
                "a"),
                new BoxScore(0, 2002, 0, new Stats(), "a")));

        YearlyPlayerStatsDao yearlyPlayerStatsDao = new YearlyPlayerStatsDao(context);
        List<YearlyPlayerStats> stats = yearlyPlayerStatsDao.getPlayerStatsFromYears(0, 2000, 2002);
        assertThat(stats.get(0).gamesPlayed, is(2));
        assertThat(stats.get(0).playerStats.assists, is(25));
        assertThat(stats.size(), is(3));
    }

    @Test
    public void canSaveTeams() {
        List<Team> teamList = new ArrayList<>();
        TeamDao teamDao = new TeamDao(context);
        try {
            teamList = teamDao.getAllTeams();
        } catch (IOException | ClassNotFoundException e) {
            Log.e("MainActivity", "Could not retrieve teams", e);
            // PROBABLY JUST CRASH
        }
        if (teamList.size() == 0) {
            // Make generator, passing in possible player names
            PlayerGen playerGen = new PlayerGen(context.getString(R.string.league_player_names),
                    context.getString(R.string.league_last_names), 2016);

            // Make 10 teams;
            teamList = new ArrayList<>();
            for (int i = 0; i < 10; ++i) {
                teamList.add(new Team("Team " + i, 90, playerGen, false, "garbage"));
            }
            teamDao.saveTeams(teamList, "player team name");
        }
        List<Team> newTeamList = null;
        try {
            newTeamList = teamDao.getAllTeams();
        } catch (IOException | ClassNotFoundException e) {
            Log.e("MainActivity", "Could not retrieve teams", e);
            // PROBABLY JUST CRASH
        }
        for (Team team : newTeamList) {
            for (Player player : team.players) {
                assertThat(player.ratings.insideShooting, is(greaterThan(0)));
            }
        }
    }
}
