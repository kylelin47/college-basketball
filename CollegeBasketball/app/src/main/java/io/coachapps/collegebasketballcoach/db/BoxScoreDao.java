package io.coachapps.collegebasketballcoach.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import io.coachapps.collegebasketballcoach.models.BoxScore;

public class BoxScoreDao {
    private Context context;
    private YearlyPlayerStatsDao yearlyPlayerStatsDao;

    public BoxScoreDao(Context context) {
        this.context = context;
        yearlyPlayerStatsDao = new YearlyPlayerStatsDao(context);
    }

    public void save(List<BoxScore> boxScores) {
        try (SQLiteDatabase db = DbHelper.getInstance(context).getWritableDatabase()) {
            db.beginTransaction();
            try {
                for (BoxScore boxScore : boxScores) {
                    updateBoxScore(boxScore, db);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
        yearlyPlayerStatsDao.updateYearlyPlayerStats(boxScores);
    }

    private void updateBoxScore(BoxScore boxScore, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(Schemas.BoxScoreEntry.PLAYER, boxScore.playerId);
        values.put(Schemas.BoxScoreEntry.POINTS, boxScore.playerStats.points);
        db.insertOrThrow(Schemas.BoxScoreEntry.TABLE_NAME, null, values);
    }
}
