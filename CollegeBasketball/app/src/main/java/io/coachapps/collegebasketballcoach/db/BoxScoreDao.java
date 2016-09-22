package io.coachapps.collegebasketballcoach.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import io.coachapps.collegebasketballcoach.models.YearlyPlayerStats;
import io.coachapps.collegebasketballcoach.models.BoxScore;

public class BoxScoreDao {
    private Context context;

    public BoxScoreDao(Context context) {
        this.context = context;
    }

    public void save(BoxScore boxScore) {
        try (SQLiteDatabase db = DbHelper.getInstance(context).getWritableDatabase()) {
            updateBoxScore(boxScore, db);
            updateYearlyPlayerStats(boxScore, db);
        }
    }

    private void updateBoxScore(BoxScore boxScore, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(Schemas.BoxScoreEntry.PLAYER, boxScore.playerId);
        values.put(Schemas.BoxScoreEntry.POINTS, boxScore.points);
        db.insert(Schemas.BoxScoreEntry.TABLE_NAME, null, values);
    }

    private void updateYearlyPlayerStats(BoxScore boxScore, SQLiteDatabase db) {
        String[] projection = {
                Schemas.YearlyPlayerStatsEntry.POINTS,
                Schemas.YearlyPlayerStatsEntry.GAMES_PLAYED
        };
        String whereClause = Schemas.YearlyPlayerStatsEntry.PLAYER + "=? AND " + Schemas
                .YearlyPlayerStatsEntry.YEAR + "=?";
        String[] whereArgs = {
                String.valueOf(boxScore.playerId),
                String.valueOf(boxScore.year)
        };
        YearlyPlayerStats stats = new YearlyPlayerStats(boxScore);
        try (Cursor cursor = db.query(Schemas.YearlyPlayerStatsEntry.TABLE_NAME, projection,
                whereClause, whereArgs, null, null, null, null)) {
            if (cursor.moveToNext()) {
                addPreviousYearlyPlayerStats(cursor, stats);
            }
        }
        ContentValues values = populateYearlyPlayerStatsEntry(stats);
        db.replace(Schemas.YearlyPlayerStatsEntry.TABLE_NAME, null, values);
    }

    private ContentValues populateYearlyPlayerStatsEntry(YearlyPlayerStats stats) {
        ContentValues values = new ContentValues();
        values.put(Schemas.YearlyPlayerStatsEntry.YEAR, stats.year);
        values.put(Schemas.YearlyPlayerStatsEntry.PLAYER, stats.playerId);
        values.put(Schemas.YearlyPlayerStatsEntry.POINTS, stats.points);
        values.put(Schemas.YearlyPlayerStatsEntry.GAMES_PLAYED, stats.gamesPlayed);
        return values;
    }

    private void addPreviousYearlyPlayerStats(Cursor cursor, YearlyPlayerStats stats) {
        stats.points += cursor.getInt(cursor.getColumnIndexOrThrow(Schemas.YearlyPlayerStatsEntry
                .POINTS));
        stats.gamesPlayed += cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyPlayerStatsEntry.GAMES_PLAYED));
    }
}
