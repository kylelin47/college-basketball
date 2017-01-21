package io.coachapps.collegebasketballcoach.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.coachapps.collegebasketballcoach.models.GameModel;
import io.coachapps.collegebasketballcoach.models.Stats;
import io.coachapps.collegebasketballcoach.models.TeamStats;
import io.coachapps.collegebasketballcoach.util.SerializationUtil;

public class GameDao {
    private Context context;
    public GameDao(Context context) {
        this.context = context;
    }

    public void save(List<GameModel> games) {
        SQLiteDatabase db = DbHelper.getInstance(context).getWritableDatabase();
        db.beginTransaction();
        try {
            for (GameModel game : games) {
                save(game, db);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

    }
    private void save(GameModel game, SQLiteDatabase db) {
        //Log.i("GameDao","Saving game : " + game.year + "yr, " + game.week + "wk, " + game.awayTeam + " @ " + game.homeTeam);
        ContentValues values = new ContentValues();
        values.put(Schemas.GameEntry.YEAR, game.year);
        values.put(Schemas.GameEntry.WEEK, game.week);
        values.put(Schemas.GameEntry.AWAY_TEAM, game.awayTeam);
        values.put(Schemas.GameEntry.HOME_TEAM, game.homeTeam);
        values.put(Schemas.GameEntry.NUM_OT, game.numOT);
        values.put(Schemas.GameEntry.AWAY_STATS, SerializationUtil.serialize(game.awayStats));
        values.put(Schemas.GameEntry.HOME_STATS, SerializationUtil.serialize(game.homeStats));
        db.insert(Schemas.GameEntry.TABLE_NAME, null, values);
        YearlyTeamStatsDao yearlyTeamStatsDao = new YearlyTeamStatsDao(context);
        // ties are impossible
        if (game.awayStats.stats.points > game.homeStats.stats.points) {
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
                Schemas.GameEntry.HOME_TEAM,
                Schemas.GameEntry.NUM_OT
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

    public GameModel getGame(int year, int week, String homeTeam, String awayTeam) {
        //Log.i("GameDao","Getting game : " + year + "yr, " + week + "wk, " + awayTeam + " @ " + homeTeam);
        GameModel game = null;
        SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase();
        String[] projection = {
                Schemas.GameEntry.AWAY_STATS,
                Schemas.GameEntry.AWAY_TEAM,
                Schemas.GameEntry.YEAR,
                Schemas.GameEntry.WEEK,
                Schemas.GameEntry.HOME_STATS,
                Schemas.GameEntry.HOME_TEAM,
                Schemas.GameEntry.NUM_OT
        };

        String whereClause = Schemas.GameEntry.HOME_TEAM + "=? AND "
                + Schemas.GameEntry.AWAY_TEAM + "=? AND "
                + Schemas.GameEntry.YEAR + "=? AND "
                + Schemas.GameEntry.WEEK + "=?";
        String[] whereArgs = {
                String.valueOf(homeTeam),
                String.valueOf(awayTeam),
                String.valueOf(year),
                String.valueOf(week)
        };

        try (Cursor cursor = db.query(Schemas.GameEntry.TABLE_NAME, projection,
                whereClause, whereArgs, null, null, null, null)) {
            if (cursor.moveToNext()) {
                game = fetchGame(cursor);
            }
        }

        return game;
    }

    public GameModel getGame(int year, int week, String team) {
        GameModel game = null;
        SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase();
        String[] projection = {
                Schemas.GameEntry.AWAY_STATS,
                Schemas.GameEntry.AWAY_TEAM,
                Schemas.GameEntry.YEAR,
                Schemas.GameEntry.WEEK,
                Schemas.GameEntry.HOME_STATS,
                Schemas.GameEntry.HOME_TEAM,
                Schemas.GameEntry.NUM_OT
        };

        String whereClause = "(" + Schemas.GameEntry.HOME_TEAM + "=? OR "
                + Schemas.GameEntry.AWAY_TEAM + "=?) AND "
                + Schemas.GameEntry.YEAR + "=? AND "
                + Schemas.GameEntry.WEEK + "=?";
        String[] whereArgs = {
                String.valueOf(team),
                String.valueOf(team),
                String.valueOf(year),
                String.valueOf(week)
        };

        try (Cursor cursor = db.query(Schemas.GameEntry.TABLE_NAME, projection,
                whereClause, whereArgs, null, null, null, null)) {
            if (cursor.moveToNext()) {
                game = fetchGame(cursor);
            }
        }

        return game;
    }

    public List<GameModel> getRecentMatchups(String teamA, String teamB, int numMatchups) {
        List<GameModel> games = new ArrayList<>();
        SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase();
        String[] projection = {
                Schemas.GameEntry.AWAY_STATS,
                Schemas.GameEntry.AWAY_TEAM,
                Schemas.GameEntry.YEAR,
                Schemas.GameEntry.WEEK,
                Schemas.GameEntry.HOME_STATS,
                Schemas.GameEntry.HOME_TEAM,
                Schemas.GameEntry.NUM_OT
        };
        String whereClause =
                "(" + Schemas.GameEntry.HOME_TEAM + "=? AND " + Schemas.GameEntry.AWAY_TEAM + " =?) OR (" +
                Schemas.GameEntry.AWAY_TEAM + "=? AND " + Schemas.GameEntry.HOME_TEAM + " =?)";
        String[] whereArgs = {
                String.valueOf(teamA),
                String.valueOf(teamB),
                String.valueOf(teamA),
                String.valueOf(teamB)
        };
        String orderBy = Schemas.GameEntry.YEAR + " DESC";
        try (Cursor cursor = db.query(Schemas.GameEntry.TABLE_NAME, projection,
                whereClause, whereArgs, null, null, orderBy, String.valueOf(numMatchups))) {
            while (cursor.moveToNext()) {
                games.add(fetchGame(cursor));
            }
        }

        Collections.sort(games, new Comparator<GameModel>() {
            @Override
            public int compare(GameModel g1, GameModel g2) {
                if (g1.year != g2.year) return g2.year - g1.year;
                return g2.week - g1.week;
            }
        });

        return games;
    }

    private GameModel fetchGame(Cursor cursor) {
        int year = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas.GameEntry.YEAR));
        int week = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas.GameEntry.WEEK));
        int numOT = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas.GameEntry.NUM_OT));
        TeamStats homeStats = (TeamStats) SerializationUtil.deserialize(cursor.getBlob(cursor
                .getColumnIndexOrThrow(Schemas.GameEntry.HOME_STATS)));
        TeamStats awayStats = (TeamStats) SerializationUtil.deserialize(cursor.getBlob(cursor
                .getColumnIndexOrThrow(Schemas.GameEntry.AWAY_STATS)));
        String homeTeam = cursor.getString(cursor.getColumnIndexOrThrow(Schemas.GameEntry
                .HOME_TEAM));
        String awayTeam = cursor.getString(cursor.getColumnIndexOrThrow(Schemas.GameEntry
                .AWAY_TEAM));
        return new GameModel(homeTeam, awayTeam, year, week, homeStats, awayStats, numOT);
    }
}
