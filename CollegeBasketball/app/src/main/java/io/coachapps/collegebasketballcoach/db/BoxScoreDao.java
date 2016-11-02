package io.coachapps.collegebasketballcoach.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import io.coachapps.collegebasketballcoach.models.BoxScore;
import io.coachapps.collegebasketballcoach.models.Stats;
import io.coachapps.collegebasketballcoach.util.SerializationUtil;

public class BoxScoreDao {
    private Context context;
    private YearlyPlayerStatsDao yearlyPlayerStatsDao;

    public BoxScoreDao(Context context) {
        this.context = context;
        yearlyPlayerStatsDao = new YearlyPlayerStatsDao(context);
    }

    public void save(List<BoxScore> boxScores) {
        SQLiteDatabase db = DbHelper.getInstance(context).getWritableDatabase();
        db.beginTransaction();
        try {
            for (BoxScore boxScore : boxScores) {
                updateBoxScore(boxScore, db);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        yearlyPlayerStatsDao.updateYearlyPlayerStats(boxScores);
    }

    /**
     * Get list of boxscores from the database for a particular game
     * @param year what year the game was played
     * @param week what week the game was
     * @param teamName name of one of the teams that played
     * @return list of boxScores for that team
     */
    public List<BoxScore> getBoxScoresFromGame(int year, int week, String teamName) {
        SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase();
        String[] projection = {
                Schemas.BoxScoreEntry.PLAYER,
                Schemas.BoxScoreEntry.YEAR,
                Schemas.BoxScoreEntry.WEEK,
                Schemas.BoxScoreEntry.STATS,
                Schemas.BoxScoreEntry.TEAM_NAME,
        };
        String whereClause = Schemas.BoxScoreEntry.YEAR + "=? AND " +
                Schemas.BoxScoreEntry.WEEK + "=? AND " +
                Schemas.BoxScoreEntry.TEAM_NAME + "=?";

        List<BoxScore> boxScores = new ArrayList<>();
            String[] whereArgs = {
                    String.valueOf(year),
                    String.valueOf(week),
                    teamName
            };
        try (Cursor cursor = db.query(Schemas.BoxScoreEntry.TABLE_NAME, projection,
                whereClause, whereArgs, null, null, null, null)) {
            while (cursor.moveToNext()) {
                boxScores.add(fetchBoxScore(cursor));
            }
        }
        return boxScores;
    }

    /**
     * Get list of boxscores from the database for a particular game
     * @param year what year the game was played
     * @param playerID player of boxscores
     * @return list of boxScores for that player
     */
    public List<BoxScore> getBoxScoresForPlayer(int year, int playerID) {
        SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase();
        String[] projection = {
                Schemas.BoxScoreEntry.PLAYER,
                Schemas.BoxScoreEntry.YEAR,
                Schemas.BoxScoreEntry.WEEK,
                Schemas.BoxScoreEntry.STATS,
                Schemas.BoxScoreEntry.TEAM_NAME,
        };
        String whereClause = Schemas.BoxScoreEntry.YEAR + "=? AND " +
                Schemas.BoxScoreEntry.PLAYER + "=?";

        List<BoxScore> boxScores = new ArrayList<>();
        String[] whereArgs = {
                String.valueOf(year),
                String.valueOf(playerID)
        };
        try (Cursor cursor = db.query(Schemas.BoxScoreEntry.TABLE_NAME, projection,
                whereClause, whereArgs, null, null, null, null)) {
            while (cursor.moveToNext()) {
                boxScores.add(fetchBoxScore(cursor));
            }
        }
        return boxScores;
    }

    private void updateBoxScore(BoxScore boxScore, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(Schemas.BoxScoreEntry.PLAYER, boxScore.playerId);
        values.put(Schemas.BoxScoreEntry.YEAR, boxScore.year);
        values.put(Schemas.BoxScoreEntry.WEEK, boxScore.week);
        values.put(Schemas.BoxScoreEntry.STATS, SerializationUtil.serialize(boxScore.playerStats));
        values.put(Schemas.BoxScoreEntry.TEAM_NAME, boxScore.teamName);
        db.insertOrThrow(Schemas.BoxScoreEntry.TABLE_NAME, null, values);
    }

    private BoxScore fetchBoxScore(Cursor cursor) {
        int playerID = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas.BoxScoreEntry.PLAYER));
        int year = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas.BoxScoreEntry.YEAR));
        int week = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas.BoxScoreEntry.WEEK));
        Stats playerStats = (Stats) SerializationUtil.deserialize(cursor.getBlob(cursor
                .getColumnIndexOrThrow(Schemas.BoxScoreEntry.STATS)));
        String teamName = cursor.getString(cursor.getColumnIndexOrThrow(Schemas.BoxScoreEntry
                .TEAM_NAME));
        return new BoxScore(playerID, year, week, playerStats, teamName);
    }
}
