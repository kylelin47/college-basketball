package io.coachapps.collegebasketballcoach.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import io.coachapps.collegebasketballcoach.models.Stats;
import io.coachapps.collegebasketballcoach.models.YearlyTeamStats;


public class YearlyTeamStatsDao {
    private Context context;
    public YearlyTeamStatsDao(Context context) {
        this.context = context;
    }

    public void recordRelativeTeamRecord(String team, int year, int numWins, int numLosses, Stats stats) {
        SQLiteDatabase db = DbHelper.getInstance(context).getWritableDatabase();
        String[] projection = {
                Schemas.YearlyTeamStatsEntry.WINS,
                Schemas.YearlyTeamStatsEntry.LOSSES,
                Schemas.YearlyTeamStatsEntry.YEAR,
                Schemas.YearlyTeamStatsEntry.POINTS,
                Schemas.YearlyTeamStatsEntry.ASSISTS,
                Schemas.YearlyTeamStatsEntry.REBOUNDS
        };
        String whereClause = Schemas.YearlyTeamStatsEntry.TEAM + "=?";
        String[] whereArgs = {
                team
        };
        YearlyTeamStats teamStats;
        try (Cursor cursor = db.query(Schemas.YearlyTeamStatsEntry.TABLE_NAME, projection,
                whereClause, whereArgs, null, null, null, null)) {
            if (cursor.moveToNext()) {
                teamStats = fetchYearlyTeamStats(cursor, team);
                teamStats.wins += numWins;
                teamStats.losses += numLosses;
                teamStats.points += stats.points;
                teamStats.assists += stats.assists;
                teamStats.rebounds += stats.defensiveRebounds + stats.offensiveRebounds;
            } else {
                teamStats = new YearlyTeamStats(team, year, numWins, numLosses, stats);
            }
        }
        ContentValues values = populateYearlyTeamStatsEntry(teamStats, team);
        db.replace(Schemas.YearlyTeamStatsEntry.TABLE_NAME, null, values);
    }
    private ContentValues populateYearlyTeamStatsEntry(YearlyTeamStats stats, String team) {
        ContentValues values = new ContentValues();
        values.put(Schemas.YearlyTeamStatsEntry.TEAM, team);
        values.put(Schemas.YearlyTeamStatsEntry.YEAR, stats.year);
        values.put(Schemas.YearlyTeamStatsEntry.WINS, stats.wins);
        values.put(Schemas.YearlyTeamStatsEntry.POINTS, stats.points);
        values.put(Schemas.YearlyTeamStatsEntry.ASSISTS, stats.assists);
        values.put(Schemas.YearlyTeamStatsEntry.REBOUNDS, stats.rebounds);
        values.put(Schemas.YearlyTeamStatsEntry.LOSSES, stats.losses);
        return values;
    }

    public List<YearlyTeamStats> getTeamStatsOfYear(int year) {
        List<YearlyTeamStats> stats = new ArrayList<>();
        SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase();
        String[] projection = {
                Schemas.YearlyTeamStatsEntry.TEAM,
                Schemas.YearlyTeamStatsEntry.WINS,
                Schemas.YearlyTeamStatsEntry.LOSSES,
                Schemas.YearlyTeamStatsEntry.YEAR,
                Schemas.YearlyTeamStatsEntry.POINTS,
                Schemas.YearlyTeamStatsEntry.ASSISTS,
                Schemas.YearlyTeamStatsEntry.REBOUNDS
        };
        String whereClause = Schemas.YearlyTeamStatsEntry.YEAR + " = ?";
        String[] whereArgs = {
                String.valueOf(year)
        };
        String orderBy = Schemas.YearlyTeamStatsEntry.WINS + " DESC";
        try (Cursor cursor = db.query(Schemas.YearlyTeamStatsEntry.TABLE_NAME, projection,
                whereClause, whereArgs, null, null, orderBy, null)) {
            while (cursor.moveToNext()) {
                stats.add(fetchYearlyTeamStats(cursor));
            }
        }
        return stats;
    }
    public List<YearlyTeamStats> getTeamStatsFromYears(String team, int beginYear, int
            endYear) {
        List<YearlyTeamStats> stats = new ArrayList<>();
        SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase();
        String[] projection = {
                Schemas.YearlyTeamStatsEntry.WINS,
                Schemas.YearlyTeamStatsEntry.LOSSES,
                Schemas.YearlyTeamStatsEntry.YEAR,
                Schemas.YearlyTeamStatsEntry.POINTS,
                Schemas.YearlyTeamStatsEntry.ASSISTS,
                Schemas.YearlyTeamStatsEntry.REBOUNDS
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
    private YearlyTeamStats fetchYearlyTeamStats(Cursor cursor) {
        return fetchYearlyTeamStats(cursor, cursor.getString(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.TEAM)));
    }
    private YearlyTeamStats fetchYearlyTeamStats(Cursor cursor, String team) {
        YearlyTeamStats stats = new YearlyTeamStats(team);
        stats.wins = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas.YearlyTeamStatsEntry
                .WINS));
        stats.losses = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.LOSSES));
        stats.year = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.YEAR));
        stats.points = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.POINTS));
        stats.assists = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.ASSISTS));
        stats.rebounds = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.REBOUNDS));
        return stats;
    }
}
