package io.coachapps.collegebasketballcoach.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
    private static DbHelper singletonInstance;
    public static synchronized DbHelper getInstance(Context context) {
        if (singletonInstance == null) {
            singletonInstance = new DbHelper(context.getApplicationContext());
        }
        return singletonInstance;
    }
    private DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static final int DATABASE_VERSION = 19;
    public static final String DATABASE_NAME = "CollegeBasketball.db";

    private static final String INTEGER_TYPE = " INTEGER";
    private static final String BLOB_TYPE = " BLOB";
    private static final String TEXT_TYPE = " TEXT";

    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES_GAME =
            "CREATE TABLE " + Schemas.GameEntry.TABLE_NAME + " (" +
                    Schemas.GameEntry._ID + INTEGER_TYPE + COMMA_SEP +
                    Schemas.GameEntry.YEAR + INTEGER_TYPE + COMMA_SEP +
                    Schemas.GameEntry.WEEK + INTEGER_TYPE + COMMA_SEP +
                    Schemas.GameEntry.AWAY_STATS + BLOB_TYPE + COMMA_SEP +
                    Schemas.GameEntry.HOME_STATS + BLOB_TYPE + COMMA_SEP +
                    Schemas.GameEntry.AWAY_TEAM + TEXT_TYPE + COMMA_SEP +
                    Schemas.GameEntry.HOME_TEAM + TEXT_TYPE + COMMA_SEP +
                    Schemas.GameEntry.NUM_OT + INTEGER_TYPE + COMMA_SEP +
                    "PRIMARY KEY (" + Schemas.GameEntry.YEAR + COMMA_SEP + Schemas
                    .GameEntry.WEEK + COMMA_SEP + Schemas.GameEntry.AWAY_TEAM + COMMA_SEP +
                    Schemas.GameEntry.HOME_TEAM + "));";

    private static final String SQL_CREATE_ENTRIES_PLAYER =
            "CREATE TABLE " + Schemas.PlayerEntry.TABLE_NAME + " (" +
                    Schemas.PlayerEntry._ID + " INTEGER PRIMARY KEY," +
                    Schemas.PlayerEntry.NAME + TEXT_TYPE + COMMA_SEP +
                    Schemas.PlayerEntry.TEAM + TEXT_TYPE + COMMA_SEP +
                    Schemas.PlayerEntry.YEAR + INTEGER_TYPE + COMMA_SEP +
                    Schemas.PlayerEntry.RATINGS + BLOB_TYPE +
                    " );";

    private static final String SQL_CREATE_ENTRIES_TEAM =
            "CREATE TABLE " + Schemas.TeamEntry.TABLE_NAME + " (" +
                    Schemas.TeamEntry._ID + INTEGER_TYPE + COMMA_SEP +
                    Schemas.TeamEntry.NAME + " TEXT PRIMARY KEY," +
                    Schemas.TeamEntry.CONFERENCE + TEXT_TYPE + COMMA_SEP +
                    Schemas.TeamEntry.PRESTIGE + INTEGER_TYPE + COMMA_SEP +
                    Schemas.TeamEntry.IS_PLAYER + INTEGER_TYPE +
                    " );";

    private static final String SQL_CREATE_ENTRIES_BOX_SCORE =
            "CREATE TABLE " + Schemas.BoxScoreEntry.TABLE_NAME + " (" +
                    Schemas.BoxScoreEntry._ID + INTEGER_TYPE + COMMA_SEP +
                    Schemas.BoxScoreEntry.PLAYER + INTEGER_TYPE + COMMA_SEP +
                    Schemas.BoxScoreEntry.WEEK + INTEGER_TYPE + COMMA_SEP +
                    Schemas.BoxScoreEntry.YEAR + INTEGER_TYPE + COMMA_SEP +
                    Schemas.BoxScoreEntry.STATS + BLOB_TYPE + COMMA_SEP +
                    Schemas.BoxScoreEntry.TEAM_NAME + TEXT_TYPE + COMMA_SEP +
                    "PRIMARY KEY (" + Schemas.BoxScoreEntry.YEAR + COMMA_SEP + Schemas
                    .BoxScoreEntry.WEEK + COMMA_SEP + Schemas.BoxScoreEntry.PLAYER + "));";

    private static final String SQL_CREATE_ENTRIES_LEAGUE_RESULTS =
            "CREATE TABLE " + Schemas.LeagueResultsEntry.TABLE_NAME + " (" +
                    Schemas.LeagueResultsEntry._ID + " INTEGER PRIMARY KEY," +
                    Schemas.LeagueResultsEntry.CHAMPION + TEXT_TYPE + COMMA_SEP +
                    Schemas.LeagueResultsEntry.COWBOY_CHAMPION + TEXT_TYPE + COMMA_SEP +
                    Schemas.LeagueResultsEntry.LAKES_CHAMPION + TEXT_TYPE + COMMA_SEP +
                    Schemas.LeagueResultsEntry.MOUNTAINS_CHAMPION + TEXT_TYPE + COMMA_SEP +
                    Schemas.LeagueResultsEntry.NORTH_CHAMPION + TEXT_TYPE + COMMA_SEP +
                    Schemas.LeagueResultsEntry.PACIFIC_CHAMPION + TEXT_TYPE + COMMA_SEP +
                    Schemas.LeagueResultsEntry.SOUTH_CHAMPION + TEXT_TYPE + COMMA_SEP +
                    Schemas.LeagueResultsEntry.MVP + INTEGER_TYPE + COMMA_SEP +
                    Schemas.LeagueResultsEntry.DPOY + INTEGER_TYPE + COMMA_SEP +
                    Schemas.LeagueResultsEntry.ALL_AMERCANS + BLOB_TYPE + COMMA_SEP +
                    Schemas.LeagueResultsEntry.ALL_COWBOY + BLOB_TYPE + COMMA_SEP +
                    Schemas.LeagueResultsEntry.ALL_LAKES + BLOB_TYPE + COMMA_SEP +
                    Schemas.LeagueResultsEntry.ALL_MOUNTAINS + BLOB_TYPE + COMMA_SEP +
                    Schemas.LeagueResultsEntry.ALL_NORTH + BLOB_TYPE + COMMA_SEP +
                    Schemas.LeagueResultsEntry.ALL_PACIFIC + BLOB_TYPE + COMMA_SEP +
                    Schemas.LeagueResultsEntry.ALL_SOUTH + BLOB_TYPE + COMMA_SEP +
                    Schemas.LeagueResultsEntry.YEAR + INTEGER_TYPE +
                    " );";

    private static final String SQL_CREATE_ENTRIES_YEARLY_TEAM_STATS =
            "CREATE TABLE " + Schemas.YearlyTeamStatsEntry.TABLE_NAME + " (" +
                    Schemas.YearlyTeamStatsEntry._ID + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.LOSSES + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.WINS + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.YEAR + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.SUMMARY + TEXT_TYPE + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.POINTS + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.ASSISTS + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.REBOUNDS + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.STEALS + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.BLOCKS + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.TURNOVERS + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.FGM + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.FGA + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.THREEPM + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.THREEPA + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.FTM + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.FTA + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.OPP_POINTS + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.OPP_ASSISTS + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.OPP_REBOUNDS + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.OPP_STEALS + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.OPP_BLOCKS + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.OPP_TURNOVERS + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.OPP_FGM + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.OPP_FGA + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.OPP_THREEPM + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.OPP_THREEPA + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.OPP_FTM + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.OPP_FTA + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyTeamStatsEntry.TEAM + TEXT_TYPE + COMMA_SEP +
                    "PRIMARY KEY (" + Schemas.YearlyTeamStatsEntry.YEAR + COMMA_SEP + Schemas
                                         .YearlyTeamStatsEntry.TEAM + "));";

    private static final String SQL_CREATE_ENTRIES_YEARLY_PLAYER_STATS =
            "CREATE TABLE " + Schemas.YearlyPlayerStatsEntry.TABLE_NAME + " (" +
                    Schemas.YearlyPlayerStatsEntry._ID + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyPlayerStatsEntry.PLAYER + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyPlayerStatsEntry.YEAR + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyPlayerStatsEntry.GAMES_PLAYED + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyPlayerStatsEntry.POINTS + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyPlayerStatsEntry.ASSISTS + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyPlayerStatsEntry.BLOCKS + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyPlayerStatsEntry.DEFENSIVE_REBOUNDS + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyPlayerStatsEntry.OFFENSIVE_REBOUNDS + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyPlayerStatsEntry.THREE_POINTS_ATTEMPTED + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyPlayerStatsEntry.THREE_POINTS_MADE + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyPlayerStatsEntry.FGA + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyPlayerStatsEntry.FGM + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyPlayerStatsEntry.FOULS + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyPlayerStatsEntry.FTA + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyPlayerStatsEntry.FTM + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyPlayerStatsEntry.STEALS + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyPlayerStatsEntry.TURNOVERS + INTEGER_TYPE + COMMA_SEP +
                    Schemas.YearlyPlayerStatsEntry.SECONDS_PLAYED + INTEGER_TYPE + COMMA_SEP +
                    "PRIMARY KEY (" + Schemas.YearlyPlayerStatsEntry.PLAYER + COMMA_SEP + Schemas
                    .YearlyPlayerStatsEntry.YEAR + "));";

    private static final String SQL_DELETE_ENTRIES_GAME =
            "DROP TABLE IF EXISTS " + Schemas.GameEntry.TABLE_NAME + ";";

    private static final String SQL_DELETE_ENTRIES_PLAYER =
            "DROP TABLE IF EXISTS " + Schemas.PlayerEntry.TABLE_NAME + ";";

    private static final String SQL_DELETE_ENTRIES_TEAM =
            "DROP TABLE IF EXISTS " + Schemas.TeamEntry.TABLE_NAME + ";";

    private static final String SQL_DELETE_ENTRIES_BOX_SCORE =
            "DROP TABLE IF EXISTS " + Schemas.BoxScoreEntry.TABLE_NAME + ";";

    private static final String SQL_DELETE_ENTRIES_LEAGUE_RESULTS =
            "DROP TABLE IF EXISTS " + Schemas.LeagueResultsEntry.TABLE_NAME + ";";

    private static final String SQL_DELETE_ENTRIES_YEARLY_TEAM_STATS =
            "DROP TABLE IF EXISTS " + Schemas.YearlyTeamStatsEntry.TABLE_NAME + ";";

    private static final String SQL_DELETE_ENTRIES_YEARLY_PLAYER_STATS =
            "DROP TABLE IF EXISTS " + Schemas.YearlyPlayerStatsEntry.TABLE_NAME + ";";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES_GAME);
        db.execSQL(SQL_CREATE_ENTRIES_PLAYER);
        db.execSQL(SQL_CREATE_ENTRIES_TEAM);
        db.execSQL(SQL_CREATE_ENTRIES_BOX_SCORE);
        db.execSQL(SQL_CREATE_ENTRIES_LEAGUE_RESULTS);
        db.execSQL(SQL_CREATE_ENTRIES_YEARLY_TEAM_STATS);
        db.execSQL(SQL_CREATE_ENTRIES_YEARLY_PLAYER_STATS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        resetDb(db);
    }

    public void resetDb(SQLiteDatabase db) {
        db.execSQL(SQL_DELETE_ENTRIES_GAME);
        db.execSQL(SQL_DELETE_ENTRIES_PLAYER);
        db.execSQL(SQL_DELETE_ENTRIES_TEAM);
        db.execSQL(SQL_DELETE_ENTRIES_BOX_SCORE);
        db.execSQL(SQL_DELETE_ENTRIES_LEAGUE_RESULTS);
        db.execSQL(SQL_DELETE_ENTRIES_YEARLY_TEAM_STATS);
        db.execSQL(SQL_DELETE_ENTRIES_YEARLY_PLAYER_STATS);
        onCreate(db);
    }
}
