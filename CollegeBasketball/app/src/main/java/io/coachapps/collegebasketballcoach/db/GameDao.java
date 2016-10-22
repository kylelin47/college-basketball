package io.coachapps.collegebasketballcoach.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import io.coachapps.collegebasketballcoach.models.GameModel;
import io.coachapps.collegebasketballcoach.models.Stats;
import io.coachapps.collegebasketballcoach.util.SerializationUtil;

public class GameDao {
    private Context context;
    public GameDao(Context context) {
        this.context = context;
    }
    public void save(GameModel game) {
        SQLiteDatabase db = DbHelper.getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Schemas.GameEntry.YEAR, game.year);
        values.put(Schemas.GameEntry.WEEK, game.week);
        values.put(Schemas.GameEntry.AWAY_TEAM, game.awayTeam);
        values.put(Schemas.GameEntry.HOME_TEAM, game.homeTeam);
        values.put(Schemas.GameEntry.AWAY_STATS, SerializationUtil.serialize(game.awayStats));
        values.put(Schemas.GameEntry.HOME_STATS, SerializationUtil.serialize(game.homeStats));
        db.insert(Schemas.GameEntry.TABLE_NAME, null, values);
        YearlyTeamStatsDao yearlyTeamStatsDao = new YearlyTeamStatsDao(context);
        // ties are impossible
        if (game.awayStats.points > game.homeStats.points) {
            yearlyTeamStatsDao.recordRelativeTeamRecord(game.awayTeam, game.year, 1, 0, game.awayStats);
            yearlyTeamStatsDao.recordRelativeTeamRecord(game.homeTeam, game.year, 0, 1, game.homeStats);
        } else {
            yearlyTeamStatsDao.recordRelativeTeamRecord(game.awayTeam, game.year, 0, 1, game.awayStats);
            yearlyTeamStatsDao.recordRelativeTeamRecord(game.homeTeam, game.year, 1, 0, game.homeStats);
        }
    }
    public List<GameModel> getGamesFromYears(String teamName, int beginYear, int endYear) {
        List<GameModel> games = new ArrayList<>();
        SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase();
        String[] projection = {
                Schemas.GameEntry.AWAY_STATS,
                Schemas.GameEntry.AWAY_TEAM,
                Schemas.GameEntry.YEAR,
                Schemas.GameEntry.WEEK,
                Schemas.GameEntry.HOME_STATS,
                Schemas.GameEntry.HOME_TEAM
        };
        String whereClause = "(" + Schemas.GameEntry.HOME_TEAM + "=? OR " + Schemas.GameEntry
                .AWAY_TEAM + " =?) AND " + Schemas.GameEntry.YEAR + " BETWEEN ? AND ?";
        String[] whereArgs = {
                String.valueOf(teamName),
                String.valueOf(teamName),
                String.valueOf(beginYear),
                String.valueOf(endYear)
        };
        String orderBy = Schemas.GameEntry.YEAR + " ASC";
        try (Cursor cursor = db.query(Schemas.GameEntry.TABLE_NAME, projection,
                whereClause, whereArgs, null, null, orderBy, null)) {
            while (cursor.moveToNext()) {
                games.add(fetchGame(cursor));
            }
        }
        return games;
    }
    private GameModel fetchGame(Cursor cursor) {
        int year = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas.GameEntry.YEAR));
        int week = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas.GameEntry.WEEK));
        Stats homeStats = (Stats) SerializationUtil.deserialize(cursor.getBlob(cursor
                .getColumnIndexOrThrow(Schemas.GameEntry.HOME_STATS)));
        Stats awayStats = (Stats) SerializationUtil.deserialize(cursor.getBlob(cursor
                .getColumnIndexOrThrow(Schemas.GameEntry.AWAY_STATS)));
        String homeTeam = cursor.getString(cursor.getColumnIndexOrThrow(Schemas.GameEntry
                .HOME_TEAM));
        String awayTeam = cursor.getString(cursor.getColumnIndexOrThrow(Schemas.GameEntry
                .AWAY_TEAM));
        return new GameModel(homeTeam, awayTeam, year, week, homeStats, awayStats);
    }
}
