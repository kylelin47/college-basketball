package io.coachapps.collegebasketballcoach.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import io.coachapps.collegebasketballcoach.models.LeagueResults;

public class LeagueResultsEntryDao {
    private Context context;
    public LeagueResultsEntryDao(Context context) {
        this.context = context;
    }

    public void save(int year, String championTeamName, int dpoyId, int mvpId) {
        SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(Schemas.LeagueResultsEntry.YEAR, year);
        values.put(Schemas.LeagueResultsEntry.DPOY, dpoyId);
        values.put(Schemas.LeagueResultsEntry.CHAMPION, championTeamName);
        values.put(Schemas.LeagueResultsEntry.MVP, mvpId);
        db.insert(Schemas.LeagueResultsEntry.TABLE_NAME, null, values);
    }

    public int getCurrentYear() {
        SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT MAX(" + Schemas.LeagueResultsEntry.YEAR + ") " +
                "FROM " + Schemas.LeagueResultsEntry.TABLE_NAME, null)) {
            if (cursor.moveToNext()) {
                int year = cursor.getInt(0);
                if (year > 0) {
                    return cursor.getInt(0) + 1;
                }
            }
        }
        return 2016;
    }
    /**
     * Range is inclusive [beginYear, endYear]
     */
    public List<LeagueResults> getLeagueResults(int beginYear, int endYear) {
        List<LeagueResults> results = new ArrayList<>();
        SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase();
        String[] projection = {
                Schemas.LeagueResultsEntry.YEAR,
                Schemas.LeagueResultsEntry.DPOY,
                Schemas.LeagueResultsEntry.CHAMPION,
                Schemas.LeagueResultsEntry.MVP
        };
        String whereClause = Schemas.LeagueResultsEntry.YEAR + " BETWEEN ? AND ?";
        String[] whereArgs = {
                String.valueOf(beginYear),
                String.valueOf(endYear)
        };
        String orderBy = Schemas.LeagueResultsEntry.YEAR + " ASC";
        try (Cursor cursor = db.query(Schemas.YearlyTeamStatsEntry.TABLE_NAME, projection,
                whereClause, whereArgs, null, null, orderBy, null)) {
            while (cursor.moveToNext()) {
                results.add(fetchLeagueResults(cursor));
            }
        }
        return results;
    }
    private LeagueResults fetchLeagueResults(Cursor cursor) {
        LeagueResults results = new LeagueResults();
        results.dpoyId = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas.LeagueResultsEntry
                .DPOY));
        results.mvpId = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas.LeagueResultsEntry.DPOY));
        results.championTeamName = cursor.getString(cursor.getColumnIndexOrThrow(Schemas
                .LeagueResultsEntry.CHAMPION));
        results.year = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas.LeagueResultsEntry.YEAR));
        return results;
    }
}
