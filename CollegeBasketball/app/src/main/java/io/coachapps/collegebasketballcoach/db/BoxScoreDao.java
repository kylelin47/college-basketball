package io.coachapps.collegebasketballcoach.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Locale;

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
            updateYearlyPlayerStats(boxScore, db);
        }
    }

    private void updateYearlyPlayerStats(BoxScore boxScore, SQLiteDatabase db) {
        try (Cursor cursor = db.rawQuery(String.format(Locale.ENGLISH, "SELECT 1 FROM %s WHERE %s" +
                "= ? AND %s = ?", Schemas.YearlyPlayerStatsEntry.TABLE_NAME, Schemas
                .YearlyPlayerStatsEntry.PLAYER, Schemas.YearlyPlayerStatsEntry.YEAR), new
                String[] {String.valueOf(boxScore.playerId), String.valueOf(boxScore.year)})) {
            if (cursor.getCount() == 0) {
                ContentValues values = new ContentValues();
                values.put(Schemas.YearlyPlayerStatsEntry.YEAR, boxScore.year);
                values.put(Schemas.YearlyPlayerStatsEntry.PLAYER, boxScore.playerId);
                values.put(Schemas.YearlyPlayerStatsEntry.POINTS, boxScore.points);
                db.insert(Schemas.YearlyPlayerStatsEntry.TABLE_NAME, null, values);
                return;
            }
        }
        db.execSQL(String.format(Locale.ENGLISH, "UPDATE %s SET %s = %s + %d, %s = %s + 1 WHERE " +
                "%s = %d AND %s = %d", Schemas.YearlyPlayerStatsEntry.TABLE_NAME, Schemas.YearlyPlayerStatsEntry.POINTS,
                Schemas.YearlyPlayerStatsEntry.POINTS, boxScore.points, Schemas
                        .YearlyPlayerStatsEntry.GAMES_PLAYED, Schemas.YearlyPlayerStatsEntry.GAMES_PLAYED,
                Schemas.YearlyPlayerStatsEntry.YEAR, boxScore.year, Schemas
                        .YearlyPlayerStatsEntry.PLAYER, boxScore.playerId));
    }
}
