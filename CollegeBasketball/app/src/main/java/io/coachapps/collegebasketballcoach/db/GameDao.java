package io.coachapps.collegebasketballcoach.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import io.coachapps.collegebasketballcoach.models.Game;
import io.coachapps.collegebasketballcoach.util.SerializationUtil;

public class GameDao {
    private Context context;
    public GameDao(Context context) {
        this.context = context;
    }
    public void save(Game game) {
        try (SQLiteDatabase db = DbHelper.getInstance(context).getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(Schemas.GameEntry.AWAY_TEAM, game.awayTeam);
            values.put(Schemas.GameEntry.HOME_TEAM, game.homeTeam);
            values.put(Schemas.GameEntry.AWAY_STATS, SerializationUtil.serialize(game.awayStats));
            values.put(Schemas.GameEntry.HOME_STATS, SerializationUtil.serialize(game.homeStats));
            db.insert(Schemas.GameEntry.TABLE_NAME, null, values);
        }
    }

}
