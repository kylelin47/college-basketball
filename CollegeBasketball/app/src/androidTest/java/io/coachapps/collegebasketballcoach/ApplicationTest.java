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
        boxScoreDao.save(new BoxScore());
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
        db.close();
        assertThat(points.size(), is(1));
        assertThat(points.get(0), is(20));
    }
}
