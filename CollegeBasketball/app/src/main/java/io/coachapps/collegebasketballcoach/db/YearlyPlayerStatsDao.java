package io.coachapps.collegebasketballcoach.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import io.coachapps.collegebasketballcoach.models.YearlyPlayerStats;

public class YearlyPlayerStatsDao {
    private Context context;
    public YearlyPlayerStatsDao(Context context) {
        this.context = context;
    }
    public List<YearlyPlayerStats> getPlayerStatsFromYears(int playerId, int beginYear, int
            endYear) {
        List<YearlyPlayerStats> stats = new ArrayList<>();
        try (SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase()) {
            String[] projection = {
                    Schemas.YearlyPlayerStatsEntry.POINTS,
                    Schemas.YearlyPlayerStatsEntry.GAMES_PLAYED,
                    Schemas.YearlyPlayerStatsEntry.YEAR
            };
            String whereClause = Schemas.YearlyPlayerStatsEntry.PLAYER + "=? AND " + Schemas
                    .YearlyPlayerStatsEntry.YEAR + " BETWEEN ? AND ?";
            String[] whereArgs = {
                    String.valueOf(playerId),
                    String.valueOf(beginYear),
                    String.valueOf(endYear)
            };
            try (Cursor cursor = db.query(Schemas.YearlyPlayerStatsEntry.TABLE_NAME, projection,
                    whereClause, whereArgs, null, null, null, null)) {
                while (cursor.moveToNext()) {
                    stats.add(fetchYearlyPlayerStats(cursor, playerId));
                }
            }
        }
        return stats;
    }
    private YearlyPlayerStats fetchYearlyPlayerStats(Cursor cursor, int playerId) {
        YearlyPlayerStats stats = new YearlyPlayerStats();
        stats.playerId = playerId;
        stats.points = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas.YearlyPlayerStatsEntry
                .POINTS));
        stats.gamesPlayed = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyPlayerStatsEntry.GAMES_PLAYED));
        stats.year = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyPlayerStatsEntry.YEAR));
        return stats;
    }
}
