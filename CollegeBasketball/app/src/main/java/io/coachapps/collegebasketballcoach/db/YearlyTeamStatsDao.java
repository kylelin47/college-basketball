package io.coachapps.collegebasketballcoach.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import io.coachapps.collegebasketballcoach.models.Stats;
import io.coachapps.collegebasketballcoach.models.TeamStats;
import io.coachapps.collegebasketballcoach.models.YearlyTeamStats;


public class YearlyTeamStatsDao {
    private Context context;
    public YearlyTeamStatsDao(Context context) {
        this.context = context;
    }

    public void recordRelativeTeamRecord(String team, int year, int numWins, int numLosses, TeamStats stats) {
        SQLiteDatabase db = DbHelper.getInstance(context).getWritableDatabase();
        String[] projection = {
                Schemas.YearlyTeamStatsEntry.WINS,
                Schemas.YearlyTeamStatsEntry.LOSSES,
                Schemas.YearlyTeamStatsEntry.YEAR,
                Schemas.YearlyTeamStatsEntry.POINTS,
                Schemas.YearlyTeamStatsEntry.ASSISTS,
                Schemas.YearlyTeamStatsEntry.REBOUNDS,
                Schemas.YearlyTeamStatsEntry.STEALS,
                Schemas.YearlyTeamStatsEntry.BLOCKS,
                Schemas.YearlyTeamStatsEntry.TURNOVERS,
                Schemas.YearlyTeamStatsEntry.FGM,
                Schemas.YearlyTeamStatsEntry.FGA,
                Schemas.YearlyTeamStatsEntry.THREEPM,
                Schemas.YearlyTeamStatsEntry.THREEPA,
                Schemas.YearlyTeamStatsEntry.FTM,
                Schemas.YearlyTeamStatsEntry.FTA,
                Schemas.YearlyTeamStatsEntry.OPP_POINTS,
                Schemas.YearlyTeamStatsEntry.OPP_ASSISTS,
                Schemas.YearlyTeamStatsEntry.OPP_REBOUNDS,
                Schemas.YearlyTeamStatsEntry.OPP_STEALS,
                Schemas.YearlyTeamStatsEntry.OPP_BLOCKS,
                Schemas.YearlyTeamStatsEntry.OPP_TURNOVERS,
                Schemas.YearlyTeamStatsEntry.OPP_FGM,
                Schemas.YearlyTeamStatsEntry.OPP_FGA,
                Schemas.YearlyTeamStatsEntry.OPP_THREEPM,
                Schemas.YearlyTeamStatsEntry.OPP_THREEPA,
                Schemas.YearlyTeamStatsEntry.OPP_FTM,
                Schemas.YearlyTeamStatsEntry.OPP_FTA
        };
        String whereClause = Schemas.YearlyTeamStatsEntry.TEAM + "=? AND " + Schemas
                .YearlyTeamStatsEntry.YEAR + "=?";
        String[] whereArgs = {
                team,
                String.valueOf(year)
        };
        YearlyTeamStats teamStats;
        try (Cursor cursor = db.query(Schemas.YearlyTeamStatsEntry.TABLE_NAME, projection,
                whereClause, whereArgs, null, null, null, null)) {
            if (cursor.moveToNext()) {
                teamStats = fetchYearlyTeamStats(cursor, team);
                teamStats.wins += numWins;
                teamStats.losses += numLosses;
                teamStats.points += stats.stats.points;
                teamStats.assists += stats.stats.assists;
                teamStats.rebounds += stats.stats.defensiveRebounds
                        + stats.stats.offensiveRebounds;
                teamStats.steals += stats.stats.steals;
                teamStats.blocks += stats.stats.blocks;
                teamStats.turnovers += stats.stats.turnovers;
                teamStats.fgm += stats.stats.fieldGoalsMade;
                teamStats.fga += stats.stats.fieldGoalsAttempted;
                teamStats.threePM += stats.stats.threePointsMade;
                teamStats.threePA += stats.stats.threePointsAttempted;
                teamStats.ftm += stats.stats.freeThrowsMade;
                teamStats.fta += stats.stats.freeThrowsAttempted;

                teamStats.opp_points += stats.oppStats.points;
                teamStats.opp_assists += stats.oppStats.assists;
                teamStats.opp_rebounds += stats.oppStats.defensiveRebounds
                        + stats.oppStats.offensiveRebounds;
                teamStats.opp_steals += stats.oppStats.steals;
                teamStats.opp_blocks += stats.oppStats.blocks;
                teamStats.opp_turnovers += stats.oppStats.turnovers;
                teamStats.opp_fgm += stats.oppStats.fieldGoalsMade;
                teamStats.opp_fga += stats.oppStats.fieldGoalsAttempted;
                teamStats.opp_threePM += stats.oppStats.threePointsMade;
                teamStats.opp_threePA += stats.oppStats.threePointsAttempted;
                teamStats.opp_ftm += stats.oppStats.freeThrowsMade;
                teamStats.opp_fta += stats.oppStats.freeThrowsAttempted;
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
        values.put(Schemas.YearlyTeamStatsEntry.LOSSES, stats.losses);
        values.put(Schemas.YearlyTeamStatsEntry.POINTS, stats.points);
        values.put(Schemas.YearlyTeamStatsEntry.ASSISTS, stats.assists);
        values.put(Schemas.YearlyTeamStatsEntry.REBOUNDS, stats.rebounds);
        values.put(Schemas.YearlyTeamStatsEntry.STEALS, stats.steals);
        values.put(Schemas.YearlyTeamStatsEntry.BLOCKS, stats.blocks);
        values.put(Schemas.YearlyTeamStatsEntry.TURNOVERS, stats.turnovers);
        values.put(Schemas.YearlyTeamStatsEntry.FGM, stats.fgm);
        values.put(Schemas.YearlyTeamStatsEntry.FGA, stats.fga);
        values.put(Schemas.YearlyTeamStatsEntry.THREEPM, stats.threePM);
        values.put(Schemas.YearlyTeamStatsEntry.THREEPA, stats.threePA);
        values.put(Schemas.YearlyTeamStatsEntry.FTM, stats.ftm);
        values.put(Schemas.YearlyTeamStatsEntry.FTA, stats.fta);
        values.put(Schemas.YearlyTeamStatsEntry.OPP_POINTS, stats.opp_points);
        values.put(Schemas.YearlyTeamStatsEntry.OPP_ASSISTS, stats.opp_assists);
        values.put(Schemas.YearlyTeamStatsEntry.OPP_REBOUNDS, stats.opp_rebounds);
        values.put(Schemas.YearlyTeamStatsEntry.OPP_STEALS, stats.opp_steals);
        values.put(Schemas.YearlyTeamStatsEntry.OPP_BLOCKS, stats.opp_blocks);
        values.put(Schemas.YearlyTeamStatsEntry.OPP_TURNOVERS, stats.opp_turnovers);
        values.put(Schemas.YearlyTeamStatsEntry.OPP_FGM, stats.opp_fgm);
        values.put(Schemas.YearlyTeamStatsEntry.OPP_FGA, stats.opp_fga);
        values.put(Schemas.YearlyTeamStatsEntry.OPP_THREEPM, stats.opp_threePM);
        values.put(Schemas.YearlyTeamStatsEntry.OPP_THREEPA, stats.opp_threePA);
        values.put(Schemas.YearlyTeamStatsEntry.OPP_FTM, stats.opp_ftm);
        values.put(Schemas.YearlyTeamStatsEntry.OPP_FTA, stats.opp_fta);
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
                Schemas.YearlyTeamStatsEntry.REBOUNDS,
                Schemas.YearlyTeamStatsEntry.STEALS,
                Schemas.YearlyTeamStatsEntry.BLOCKS,
                Schemas.YearlyTeamStatsEntry.TURNOVERS,
                Schemas.YearlyTeamStatsEntry.FGM,
                Schemas.YearlyTeamStatsEntry.FGA,
                Schemas.YearlyTeamStatsEntry.THREEPM,
                Schemas.YearlyTeamStatsEntry.THREEPA,
                Schemas.YearlyTeamStatsEntry.FTM,
                Schemas.YearlyTeamStatsEntry.FTA,
                Schemas.YearlyTeamStatsEntry.OPP_POINTS,
                Schemas.YearlyTeamStatsEntry.OPP_ASSISTS,
                Schemas.YearlyTeamStatsEntry.OPP_REBOUNDS,
                Schemas.YearlyTeamStatsEntry.OPP_STEALS,
                Schemas.YearlyTeamStatsEntry.OPP_BLOCKS,
                Schemas.YearlyTeamStatsEntry.OPP_TURNOVERS,
                Schemas.YearlyTeamStatsEntry.OPP_FGM,
                Schemas.YearlyTeamStatsEntry.OPP_FGA,
                Schemas.YearlyTeamStatsEntry.OPP_THREEPM,
                Schemas.YearlyTeamStatsEntry.OPP_THREEPA,
                Schemas.YearlyTeamStatsEntry.OPP_FTM,
                Schemas.YearlyTeamStatsEntry.OPP_FTA
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
                Schemas.YearlyTeamStatsEntry.REBOUNDS,
                Schemas.YearlyTeamStatsEntry.STEALS,
                Schemas.YearlyTeamStatsEntry.BLOCKS,
                Schemas.YearlyTeamStatsEntry.TURNOVERS,
                Schemas.YearlyTeamStatsEntry.FGM,
                Schemas.YearlyTeamStatsEntry.FGA,
                Schemas.YearlyTeamStatsEntry.THREEPM,
                Schemas.YearlyTeamStatsEntry.THREEPA,
                Schemas.YearlyTeamStatsEntry.FTM,
                Schemas.YearlyTeamStatsEntry.FTA,
                Schemas.YearlyTeamStatsEntry.OPP_POINTS,
                Schemas.YearlyTeamStatsEntry.OPP_ASSISTS,
                Schemas.YearlyTeamStatsEntry.OPP_REBOUNDS,
                Schemas.YearlyTeamStatsEntry.OPP_STEALS,
                Schemas.YearlyTeamStatsEntry.OPP_BLOCKS,
                Schemas.YearlyTeamStatsEntry.OPP_TURNOVERS,
                Schemas.YearlyTeamStatsEntry.OPP_FGM,
                Schemas.YearlyTeamStatsEntry.OPP_FGA,
                Schemas.YearlyTeamStatsEntry.OPP_THREEPM,
                Schemas.YearlyTeamStatsEntry.OPP_THREEPA,
                Schemas.YearlyTeamStatsEntry.OPP_FTM,
                Schemas.YearlyTeamStatsEntry.OPP_FTA
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
        stats.wins = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.WINS));
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
        stats.steals = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.STEALS));
        stats.blocks = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.BLOCKS));
        stats.turnovers = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.TURNOVERS));
        stats.fgm = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.FGM));
        stats.fga = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.FGA));
        stats.threePM = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.THREEPM));
        stats.threePA = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.THREEPA));
        stats.ftm = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.FTM));
        stats.fta = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.FTA));

        stats.opp_points = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.OPP_POINTS));
        stats.opp_assists = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.OPP_ASSISTS));
        stats.opp_rebounds = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.OPP_REBOUNDS));
        stats.opp_steals = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.OPP_STEALS));
        stats.opp_blocks = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.OPP_BLOCKS));
        stats.opp_turnovers = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.OPP_TURNOVERS));
        stats.opp_fgm = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.OPP_FGM));
        stats.opp_fga = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.OPP_FGA));
        stats.opp_threePM = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.OPP_THREEPM));
        stats.opp_threePA = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.OPP_THREEPA));
        stats.opp_ftm = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.OPP_FTM));
        stats.opp_fta = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                .YearlyTeamStatsEntry.OPP_FTA));
        return stats;
    }
}
