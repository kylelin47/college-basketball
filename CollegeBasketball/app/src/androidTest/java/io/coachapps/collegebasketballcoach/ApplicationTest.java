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
import java.util.List;

import io.coachapps.collegebasketballcoach.db.BoxScoreDao;
import io.coachapps.collegebasketballcoach.db.DbHelper;
import io.coachapps.collegebasketballcoach.db.Schemas;
import io.coachapps.collegebasketballcoach.models.BoxScore;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class ApplicationTest {
    private Context context;
    @Before
    public void getContext() {
        context = InstrumentationRegistry.getTargetContext();
    }
    @Test
    public void canSaveBoxScores() {
        BoxScoreDao boxScoreDao = new BoxScoreDao(context);
        BoxScore boxScore = new BoxScore(0, 2000);
        boxScore.playerStats.points = 25;
        //boxScoreDao.save(new BoxScore(0, 2000));
        //boxScoreDao.save(boxScore);
        //boxScoreDao.save(new BoxScore(0, 2001));
        //boxScoreDao.save(new BoxScore(0, 2002));
        SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase();
        //DbHelper.getInstance(context).resetDb(db);
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
        int num = 0;
        List<Integer> yearlyPoints = new ArrayList<>();
        try (Cursor c = db.query(Schemas.YearlyPlayerStatsEntry.TABLE_NAME, null, null, null, null,
                null, null, null)) {
            while (c.moveToNext()) {
                yearlyPoints.add(c.getInt(c.getColumnIndexOrThrow(Schemas
                        .YearlyPlayerStatsEntry.POINTS)));
                num++;
            }
        }
        //DbHelper.getInstance(context).resetDb(db);
        db.close();
        assertThat(yearlyPoints.get(0), is(25));
        assertThat(num, is(3));
        assertThat(points.size(), is(4));
        assertThat(points.get(0), is(20));
    }
}
