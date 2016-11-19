package io.coachapps.collegebasketballcoach.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceConfigurationError;

import io.coachapps.collegebasketballcoach.models.BoxScore;
import io.coachapps.collegebasketballcoach.models.YearlyPlayerStats;

public class YearlyPlayerStatsDao {
    private Context context;

    public YearlyPlayerStatsDao(Context context) {
        this.context = context;
    }

    public void updateYearlyPlayerStats(List<BoxScore> boxScores) {
        SQLiteDatabase db = DbHelper.getInstance(context).getWritableDatabase();
        db.beginTransaction();
        try {
            for (BoxScore boxScore : boxScores) {
                String whereClause = Schemas.YearlyPlayerStatsEntry.PLAYER + "=? AND " + Schemas
                        .YearlyPlayerStatsEntry.YEAR + "=?";
                String[] whereArgs = {
                        String.valueOf(boxScore.playerId),
                        String.valueOf(boxScore.year)
                };
                YearlyPlayerStats stats = new YearlyPlayerStats(boxScore);
                try (Cursor cursor = db.query(Schemas.YearlyPlayerStatsEntry.TABLE_NAME, null,
                        whereClause, whereArgs, null, null, null, null)) {
                    if (cursor.moveToNext()) {
                        addToYearlyPlayerStats(cursor, stats);
                    }
                }
                ContentValues values = populateYearlyPlayerStatsEntry(stats, boxScore.playerId);
                db.replaceOrThrow(Schemas.YearlyPlayerStatsEntry.TABLE_NAME, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private ContentValues populateYearlyPlayerStatsEntry(YearlyPlayerStats stats, int playerId) {
        ContentValues values = new ContentValues();
        values.put(Schemas.YearlyPlayerStatsEntry.YEAR, stats.year);
        values.put(Schemas.YearlyPlayerStatsEntry.PLAYER, playerId);
        values.put(Schemas.YearlyPlayerStatsEntry.GAMES_PLAYED, stats.gamesPlayed);
        values.put(Schemas.YearlyPlayerStatsEntry.POINTS, stats.playerStats.points);
        values.put(Schemas.YearlyPlayerStatsEntry.OFFENSIVE_REBOUNDS, stats.playerStats.offensiveRebounds);
        values.put(Schemas.YearlyPlayerStatsEntry.DEFENSIVE_REBOUNDS, stats.playerStats.defensiveRebounds);
        values.put(Schemas.YearlyPlayerStatsEntry.ASSISTS, stats.playerStats.assists);
        values.put(Schemas.YearlyPlayerStatsEntry.STEALS, stats.playerStats.steals);
        values.put(Schemas.YearlyPlayerStatsEntry.BLOCKS, stats.playerStats.blocks);
        values.put(Schemas.YearlyPlayerStatsEntry.TURNOVERS, stats.playerStats.turnovers);
        values.put(Schemas.YearlyPlayerStatsEntry.FOULS, stats.playerStats.fouls);
        values.put(Schemas.YearlyPlayerStatsEntry.FGA, stats.playerStats.fieldGoalsAttempted);
        values.put(Schemas.YearlyPlayerStatsEntry.FGM, stats.playerStats.fieldGoalsMade);
        values.put(Schemas.YearlyPlayerStatsEntry.THREE_POINTS_ATTEMPTED, stats
                .playerStats.threePointsAttempted);
        values.put(Schemas.YearlyPlayerStatsEntry.THREE_POINTS_MADE, stats.playerStats
                .threePointsMade);
        values.put(Schemas.YearlyPlayerStatsEntry.FTM, stats.playerStats.freeThrowsMade);
        values.put(Schemas.YearlyPlayerStatsEntry.FTA, stats.playerStats.freeThrowsAttempted);
        values.put(Schemas.YearlyPlayerStatsEntry.SECONDS_PLAYED, stats.playerStats.secondsPlayed);
        return values;
    }

    public List<YearlyPlayerStats> getPlayerStatsFromYears(int playerId, int beginYear, int
            endYear) {
        List<YearlyPlayerStats> stats = new ArrayList<>();
        SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase();
        String whereClause = Schemas.YearlyPlayerStatsEntry.PLAYER + "=? AND " + Schemas
                .YearlyPlayerStatsEntry.YEAR + " BETWEEN ? AND ?";
        String[] whereArgs = {
                String.valueOf(playerId),
                String.valueOf(beginYear),
                String.valueOf(endYear)
        };
        String orderBy = Schemas.YearlyPlayerStatsEntry.YEAR + " ASC";
        try (Cursor cursor = db.query(Schemas.YearlyPlayerStatsEntry.TABLE_NAME, null,
                whereClause, whereArgs, null, null, orderBy, null)) {
            while (cursor.moveToNext()) {
                YearlyPlayerStats existingStats = new YearlyPlayerStats(playerId);
                addToYearlyPlayerStats(cursor, existingStats);
                stats.add(existingStats);
            }
        }
        return stats;
    }

    public List<YearlyPlayerStats> getLeagueLeaders(int year, int numPlayers, String category) {
        List<YearlyPlayerStats> stats = new ArrayList<>();
        SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase();
        String whereClause = Schemas.YearlyPlayerStatsEntry.YEAR + "=?";
        String[] whereArgs = {
                String.valueOf(year)
        };
        String orderBy = category + " DESC";
        String limit = String.valueOf(numPlayers);
        try (Cursor cursor = db.query(Schemas.YearlyPlayerStatsEntry.TABLE_NAME, null,
                whereClause, whereArgs, null, null, orderBy, limit)) {
            while (cursor.moveToNext()) {
                YearlyPlayerStats existingStats = new YearlyPlayerStats(
                        cursor.getInt(cursor.getColumnIndexOrThrow(
                                Schemas.YearlyPlayerStatsEntry.PLAYER)));
                addToYearlyPlayerStats(cursor, existingStats);
                stats.add(existingStats);
            }
        }
        return stats;
    }

    public List<YearlyPlayerStats> getAllYearlyPlayerStats(int year) {
        List<YearlyPlayerStats> stats = new ArrayList<>();
        SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase();
        String whereClause = Schemas.YearlyPlayerStatsEntry.YEAR + "=?";
        String[] whereArgs = {
                String.valueOf(year)
        };
        try (Cursor cursor = db.query(Schemas.YearlyPlayerStatsEntry.TABLE_NAME, null,
                whereClause, whereArgs, null, null, null, null)) {
            while (cursor.moveToNext()) {
                YearlyPlayerStats existingStats = new YearlyPlayerStats(
                        cursor.getInt(cursor.getColumnIndexOrThrow(
                                Schemas.YearlyPlayerStatsEntry.PLAYER)));
                addToYearlyPlayerStats(cursor, existingStats);
                stats.add(existingStats);
            }
        }
        return stats;
    }

    private void addToYearlyPlayerStats(Cursor cursor, YearlyPlayerStats existingStats) {
        existingStats.year = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyPlayerStatsEntry.YEAR));

        existingStats.gamesPlayed += cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyPlayerStatsEntry.GAMES_PLAYED));
        existingStats.playerStats.points += cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyPlayerStatsEntry.POINTS));
        existingStats.playerStats.assists += cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyPlayerStatsEntry.ASSISTS));
        existingStats.playerStats.blocks += cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyPlayerStatsEntry.BLOCKS));
        existingStats.playerStats.defensiveRebounds += cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyPlayerStatsEntry.DEFENSIVE_REBOUNDS));
        existingStats.playerStats.fieldGoalsAttempted += cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyPlayerStatsEntry.FGA));
        existingStats.playerStats.fieldGoalsMade += cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyPlayerStatsEntry.FGM));
        existingStats.playerStats.fouls += cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyPlayerStatsEntry.FOULS));
        existingStats.playerStats.freeThrowsAttempted += cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyPlayerStatsEntry.FTA));
        existingStats.playerStats.freeThrowsMade += cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyPlayerStatsEntry.FTM));
        existingStats.playerStats.offensiveRebounds += cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyPlayerStatsEntry.OFFENSIVE_REBOUNDS));
        existingStats.playerStats.steals += cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyPlayerStatsEntry.STEALS));
        existingStats.playerStats.turnovers += cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyPlayerStatsEntry.TURNOVERS));
        existingStats.playerStats.threePointsAttempted += cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyPlayerStatsEntry.THREE_POINTS_ATTEMPTED));
        existingStats.playerStats.threePointsMade += cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyPlayerStatsEntry.THREE_POINTS_MADE));
        existingStats.playerStats.secondsPlayed += cursor.getInt(cursor.getColumnIndexOrThrow
                (Schemas.YearlyPlayerStatsEntry.SECONDS_PLAYED));
    }
}
