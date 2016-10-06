package io.coachapps.collegebasketballcoach;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.coachapps.collegebasketballcoach.db.BoxScoreDao;
import io.coachapps.collegebasketballcoach.db.DbHelper;
import io.coachapps.collegebasketballcoach.db.GameDao;
import io.coachapps.collegebasketballcoach.db.Schemas;
import io.coachapps.collegebasketballcoach.db.YearlyPlayerStatsDao;
import io.coachapps.collegebasketballcoach.models.BoxScore;
import io.coachapps.collegebasketballcoach.models.Game;
import io.coachapps.collegebasketballcoach.models.YearlyPlayerStats;

import static org.hamcrest.CoreMatchers.is;
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
        BoxScore boxScore = new BoxScore(0, 2000);
        boxScore.playerStats.points = 20;
        BoxScore boxScore2 = new BoxScore(0, 2000);
        boxScore2.playerStats.points = 5;

        boxScoreDao.save(Arrays.asList(boxScore, boxScore2, new BoxScore(0, 2001),
                new BoxScore(0, 2002)));
        SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase();
        String[] projection = {
                Schemas.BoxScoreEntry.POINTS
        };
        List<Integer> points = new ArrayList<>();
        try (Cursor c = db.query(Schemas.BoxScoreEntry.TABLE_NAME, projection, null, null, null,
                null, null, null)) {
            while (c.moveToNext()) {
                points.add(c.getInt(c.getColumnIndexOrThrow(Schemas.BoxScoreEntry.POINTS)));
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
        BoxScore boxScore = new BoxScore(0, 2000);
        boxScore.playerStats.assists = 20;
        BoxScore boxScore2 = new BoxScore(0, 2000);
        boxScore2.playerStats.assists = 5;

        boxScoreDao.save(Arrays.asList(boxScore, boxScore2, new BoxScore(0, 2001),
                new BoxScore(0, 2002)));

        YearlyPlayerStatsDao yearlyPlayerStatsDao = new YearlyPlayerStatsDao(context);
        List<YearlyPlayerStats> stats = yearlyPlayerStatsDao.getPlayerStatsFromYears(0, 2000, 2002);
        assertThat(stats.get(0).gamesPlayed, is(2));
        assertThat(stats.get(0).playerStats.assists, is(25));
        assertThat(stats.size(), is(3));
    }

    @Test
    public void canSaveGames() {
        GameDao gameDao = new GameDao(context);
        Game game = new Game("goods", "bads", 2000);
        game.homeStats.points = 20;
        game.awayStats.blocks = 20;
        game.homeStats.fouls = 15;
        gameDao.save(game);
    }
}
