package io.coachapps.collegebasketballcoach.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import io.coachapps.collegebasketballcoach.models.BoxScore;

public class BoxScoreDao {
    private DbHelper dbHelper;

    public BoxScoreDao(Context context) {
        dbHelper = new DbHelper(context);
    }

    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
    public void save(BoxScore boxScore) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Schemas.BoxScoreEntry.POINTS, boxScore.points);
        db.insert(Schemas.BoxScoreEntry.TABLE_NAME, null, values);
    }
}
