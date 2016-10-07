package io.coachapps.collegebasketballcoach.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import io.coachapps.collegebasketballcoach.models.YearlyTeamStats;


public class YearlyTeamStatsDao {
    private Context context;
    public YearlyTeamStatsDao(Context context) {
        this.context = context;
    }

    public void recordRelativeTeamRecord(String team, int year, int numWins, int numLosses) {
        try {
            SQLiteDatabase db = DbHelper.getInstance(context).getWritableDatabase();
            String[] projection = {
                    Schemas.YearlyTeamStatsEntry.WINS,
                    Schemas.YearlyTeamStatsEntry.LOSSES,
                    Schemas.YearlyTeamStatsEntry.YEAR
            };
            String whereClause = Schemas.YearlyTeamStatsEntry.TEAM + "=?";
            String[] whereArgs = {
                    team
            };
            YearlyTeamStats stats;
            try (Cursor cursor = db.query(Schemas.YearlyTeamStatsEntry.TABLE_NAME, projection,
                    whereClause, whereArgs, null, null, null, null)) {
                if (cursor.moveToNext()) {
                    stats = fetchYearlyTeamStats(cursor, team);
                    stats.wins += numWins;
                    stats.losses += numLosses;
                } else {
                    stats = new YearlyTeamStats(team, year, numWins, numLosses);
                }
            }
            ContentValues values = populateYearlyTeamStatsEntry(stats, team);
            db.replace(Schemas.YearlyTeamStatsEntry.TABLE_NAME, null, values);
        } finally {
            DbHelper.getInstance(context).close();
        }
    }
    private ContentValues populateYearlyTeamStatsEntry(YearlyTeamStats stats, String team) {
        ContentValues values = new ContentValues();
        values.put(Schemas.YearlyTeamStatsEntry.TEAM, team);
        values.put(Schemas.YearlyTeamStatsEntry.YEAR, stats.year);
        values.put(Schemas.YearlyTeamStatsEntry.WINS, stats.wins);
        values.put(Schemas.YearlyTeamStatsEntry.LOSSES, stats.losses);
        return values;
    }

    public List<YearlyTeamStats> getTeamStatsFromYears(String team, int beginYear, int
            endYear) {
        List<YearlyTeamStats> stats = new ArrayList<>();
        SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase();
        String[] projection = {
                Schemas.YearlyTeamStatsEntry.WINS,
                Schemas.YearlyTeamStatsEntry.LOSSES,
                Schemas.YearlyTeamStatsEntry.YEAR
        };
        String whereClause = Schemas.YearlyTeamStatsEntry.TEAM + "=? AND " + Schemas
                .YearlyTeamStatsEntry.YEAR + " BETWEEN ? AND ?";
        String[] whereArgs = {
                team,
                String.valueOf(beginYear),
                String.valueOf(endYear)
        };
        String orderBy = Schemas.YearlyTeamStatsEntry.YEAR + " ASC";
        try (Cursor cursor = db.query(Schemas.YearlyTeamStatsEntry.TABLE_NAME, projection,
                whereClause, whereArgs, null, null, orderBy, null)) {
            while (cursor.moveToNext()) {
                stats.add(fetchYearlyTeamStats(cursor, team));
            }
        }
        return stats;
    }
    private YearlyTeamStats fetchYearlyTeamStats(Cursor cursor, String team) {
        YearlyTeamStats stats = new YearlyTeamStats(team);
        stats.wins = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas.YearlyTeamStatsEntry
                .WINS));
        stats.losses = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.LOSSES));
        stats.year = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.YEAR));
        return stats;
    }
}
