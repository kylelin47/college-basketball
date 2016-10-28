package io.coachapps.collegebasketballcoach.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.coachapps.collegebasketballcoach.basketballsim.Player;
import io.coachapps.collegebasketballcoach.models.PlayerModel;
import io.coachapps.collegebasketballcoach.models.PlayerRatings;
import io.coachapps.collegebasketballcoach.util.SerializationUtil;

public class PlayerDao {
    private Context context;
    public PlayerDao(Context context) {
        this.context = context;
    }

    public List<Player> getPlayers(String teamName) throws IOException, ClassNotFoundException {
        List<Player> players = new ArrayList<>();
        SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase();
        String[] projection = {
                Schemas.PlayerEntry._ID,
                Schemas.PlayerEntry.NAME,
                Schemas.PlayerEntry.RATINGS,
                Schemas.PlayerEntry.YEAR
        };
        String whereClause = Schemas.PlayerEntry.TEAM + " = ?";
        String[] whereArgs = {
                teamName
        };
        try (Cursor c = db.query(Schemas.PlayerEntry.TABLE_NAME, projection, whereClause,
                whereArgs, null, null, null, null)) {
            while (c.moveToNext()) {
                players.add(createPlayer(c));
            }
        }
        return players;
    }

    public Player getPlayer(int playerId) {
        SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase();
        String[] projection = {
                Schemas.PlayerEntry._ID,
                Schemas.PlayerEntry.NAME,
                Schemas.PlayerEntry.RATINGS,
                Schemas.PlayerEntry.YEAR
        };
        String whereClause = Schemas.PlayerEntry._ID + " = ?";
        String[] whereArgs = {
                String.valueOf(playerId)
        };
        try (Cursor c = db.query(Schemas.PlayerEntry.TABLE_NAME, projection, whereClause,
                whereArgs, null, null, null, null)) {
            if (c.moveToNext()) {
                return createPlayer(c);
            }
        }
        return null;
    }

    public void save(PlayerModel player) {
        SQLiteDatabase db = DbHelper.getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Schemas.PlayerEntry._ID, player.id);
        values.put(Schemas.PlayerEntry.NAME, player.name);
        values.put(Schemas.PlayerEntry.TEAM, player.team);
        values.put(Schemas.PlayerEntry.YEAR, player.year);
        values.put(Schemas.PlayerEntry.RATINGS, SerializationUtil.serialize(player.ratings));
        db.insert(Schemas.PlayerEntry.TABLE_NAME, null, values);
    }

    private Player createPlayer(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas.PlayerEntry._ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(Schemas.PlayerEntry.NAME));
        int year = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas.PlayerEntry.YEAR));
        PlayerRatings ratings = (PlayerRatings) SerializationUtil.deserialize(cursor.getBlob(cursor
                    .getColumnIndexOrThrow(Schemas.PlayerEntry.RATINGS)));
        return new Player(name, ratings, null, id, year);
    }
}
