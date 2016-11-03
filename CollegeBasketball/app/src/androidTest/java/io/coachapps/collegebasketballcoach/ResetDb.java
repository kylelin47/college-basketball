package io.coachapps.collegebasketballcoach;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.coachapps.collegebasketballcoach.db.DbHelper;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class ResetDb {
    private Context context;
    @Before
    public void _init() {
        context = InstrumentationRegistry.getTargetContext();
        SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase();
        DbHelper.getInstance(context).resetDb(db);
        db.close();
    }
    @Test
    public void hack() {
        Log.i("lol", "lel");
    }
}
