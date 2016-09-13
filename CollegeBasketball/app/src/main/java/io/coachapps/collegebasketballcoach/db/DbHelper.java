package io.coachapps.collegebasketballcoach.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "CollegeBasketball.db";

    private static final String INTEGER_TYPE = " INTEGER";
    private static final String BLOB_TYPE = " BLOB";
    private static final String TEXT_TYPE = " TEXT";

    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES_GAME =
            "CREATE TABLE " + Schemas.GameEntry.TABLE_NAME + " (" +
                    Schemas.GameEntry._ID + " INTEGER PRIMARY KEY," +
                    Schemas.GameEntry.AWAY_STATS + BLOB_TYPE + COMMA_SEP +
                    Schemas.GameEntry.HOME_STATS + BLOB_TYPE + COMMA_SEP +
                    Schemas.GameEntry.AWAY_TEAM + TEXT_TYPE + COMMA_SEP +
                    Schemas.GameEntry.HOME_TEAM + TEXT_TYPE + COMMA_SEP +
                    " );";

    private static final String SQL_CREATE_ENTRIES_PLAYER =
            "CREATE TABLE " + Schemas.PlayerEntry.TABLE_NAME + " (" +
                    Schemas.PlayerEntry._ID + " INTEGER PRIMARY KEY," +
                    Schemas.PlayerEntry.NAME + TEXT_TYPE + COMMA_SEP +
                    Schemas.PlayerEntry.TEAM + TEXT_TYPE + COMMA_SEP +
                    Schemas.PlayerEntry.YEAR + INTEGER_TYPE + COMMA_SEP +
                    " );";

    private static final String SQL_CREATE_ENTRIES_TEAM =
            "CREATE TABLE " + Schemas.TeamEntry.TABLE_NAME + " (" +
                    Schemas.TeamEntry._ID + " INTEGER PRIMARY KEY," +
                    Schemas.TeamEntry.NAME + TEXT_TYPE + COMMA_SEP +
                    Schemas.TeamEntry.CONFERENCE + TEXT_TYPE + COMMA_SEP +
                    " );";

    private static final String SQL_CREATE_ENTRIES_BOX_SCORE =
            "CREATE TABLE " + Schemas.BoxScoreEntry.TABLE_NAME + " (" +
                    Schemas.BoxScoreEntry._ID + " INTEGER PRIMARY KEY," +
                    Schemas.BoxScoreEntry.PLAYER + INTEGER_TYPE +
                    Schemas.BoxScoreEntry.GAME + INTEGER_TYPE +
                    Schemas.BoxScoreEntry.POINTS + INTEGER_TYPE +
                    Schemas.BoxScoreEntry.ASSISTS + INTEGER_TYPE +
                    Schemas.BoxScoreEntry.FIELD_GOALS_MADE + INTEGER_TYPE +
                    Schemas.BoxScoreEntry.REBOUNDS + INTEGER_TYPE +
                    " );";

    private static final String SQL_CREATE_ENTRIES_LEAGUE_RESULTS =
            "CREATE TABLE " + Schemas.LeagueResultsEntry.TABLE_NAME + " (" +
                    Schemas.LeagueResultsEntry._ID + " INTEGER PRIMARY KEY" +
                    Schemas.LeagueResultsEntry.CHAMPION + TEXT_TYPE +
                    Schemas.LeagueResultsEntry.MVP + INTEGER_TYPE +
                    Schemas.LeagueResultsEntry.DPOY + INTEGER_TYPE +
                    Schemas.LeagueResultsEntry.YEAR + INTEGER_TYPE +
                    " );";

    private static final String SQL_CREATE_ENTRIES_YEARLY_TEAM_STATS =
            "CREATE TABLE " + Schemas.YearlyTeamStatsEntry.TABLE_NAME + " (" +
                    Schemas.YearlyTeamStatsEntry._ID + " INTEGER PRIMARY KEY" +
                    Schemas.YearlyTeamStatsEntry.LOSSES + INTEGER_TYPE +
                    Schemas.YearlyTeamStatsEntry.WINS + INTEGER_TYPE +
                    Schemas.YearlyTeamStatsEntry.YEAR + INTEGER_TYPE +
                    Schemas.YearlyTeamStatsEntry.TEAM + TEXT_TYPE +
                    " );";

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

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES_GAME);
        db.execSQL(SQL_CREATE_ENTRIES_PLAYER);
        db.execSQL(SQL_CREATE_ENTRIES_TEAM);
        db.execSQL(SQL_CREATE_ENTRIES_BOX_SCORE);
        db.execSQL(SQL_CREATE_ENTRIES_LEAGUE_RESULTS);
        db.execSQL(SQL_CREATE_ENTRIES_YEARLY_TEAM_STATS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES_GAME);
        db.execSQL(SQL_DELETE_ENTRIES_PLAYER);
        db.execSQL(SQL_DELETE_ENTRIES_TEAM);
        db.execSQL(SQL_DELETE_ENTRIES_BOX_SCORE);
        db.execSQL(SQL_DELETE_ENTRIES_LEAGUE_RESULTS);
        db.execSQL(SQL_DELETE_ENTRIES_YEARLY_TEAM_STATS);
        onCreate(db);
    }
}
