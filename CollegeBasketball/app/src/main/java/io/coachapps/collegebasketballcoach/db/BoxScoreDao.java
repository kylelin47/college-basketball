package io.coachapps.collegebasketballcoach.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import io.coachapps.collegebasketballcoach.models.BoxScore;

public class BoxScoreDao {
    private Context context;

    public BoxScoreDao(Context context) {
        this.context = context;
    }

    public void save(BoxScore boxScore) {
        try (SQLiteDatabase db = DbHelper.getInstance(context).getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(Schemas.BoxScoreEntry.PLAYER, boxScore.playerId);
            values.put(Schemas.BoxScoreEntry.POINTS, boxScore.points);
            db.insert(Schemas.BoxScoreEntry.TABLE_NAME, null, values);
        }
    }
}
