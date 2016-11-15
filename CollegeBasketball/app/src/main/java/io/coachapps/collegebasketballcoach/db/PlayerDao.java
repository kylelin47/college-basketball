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
        String whereClause = Schemas.PlayerEntry.TEAM + " = ? AND " +
                Schemas.PlayerEntry.YEAR + " < 5";
        String[] whereArgs = {
                teamName
        };
        try (Cursor c = db.query(Schemas.PlayerEntry.TABLE_NAME, projection, whereClause,
                whereArgs, null, null, null, null)) {
            while (c.moveToNext()) {
                Player p = createPlayer(c);
                if (p == null) {
                    System.out.println("Found null player in db for " + teamName);
                } else {
                    players.add(p);
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
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

    public void updatePlayerRatings(int playerID, PlayerRatings ratings) {
        SQLiteDatabase db = DbHelper.getInstance(context).getWritableDatabase();
        db.beginTransaction();
        System.out.println("About to update ratings, min = " + ratings.lineupMinutes + ", pos = " + ratings.lineupPosition);
        try {
            String whereClause = Schemas.PlayerEntry._ID + "=?";
            String[] whereArgs = {
                    String.valueOf(playerID),
            };
            ContentValues values = new ContentValues();
            try (Cursor cursor = db.query(Schemas.PlayerEntry.TABLE_NAME, null,
                    whereClause, whereArgs, null, null, null, null)) {
                if (cursor.moveToNext()) {
                    values.put(Schemas.PlayerEntry._ID, cursor.getInt(cursor.getColumnIndexOrThrow(
                            Schemas.PlayerEntry._ID)));
                    values.put(Schemas.PlayerEntry.NAME, cursor.getString(cursor.getColumnIndexOrThrow(
                            Schemas.PlayerEntry.NAME)));
                    values.put(Schemas.PlayerEntry.TEAM, cursor.getString(cursor.getColumnIndexOrThrow(
                            Schemas.PlayerEntry.TEAM)));
                    values.put(Schemas.PlayerEntry.YEAR, cursor.getInt(cursor.getColumnIndexOrThrow(
                            Schemas.PlayerEntry.YEAR)));
                    System.out.println("updated ratings for " +
                            cursor.getString(cursor.getColumnIndexOrThrow(Schemas.PlayerEntry.NAME)));
                }
            }
            values.put(Schemas.PlayerEntry.RATINGS, SerializationUtil.serialize(ratings));
            db.replaceOrThrow(Schemas.PlayerEntry.TABLE_NAME, null, values);
            db.setTransactionSuccessful();
            System.out.println("donezo");
        } finally {
            db.endTransaction();
        }
    }

    public void updatePlayer(PlayerModel player) {
        SQLiteDatabase db = DbHelper.getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Schemas.PlayerEntry._ID, player.id);
        values.put(Schemas.PlayerEntry.NAME, player.name);
        values.put(Schemas.PlayerEntry.TEAM, player.team);
        values.put(Schemas.PlayerEntry.YEAR, player.year);
        values.put(Schemas.PlayerEntry.RATINGS, SerializationUtil.serialize(player.ratings));
        db.replace(Schemas.PlayerEntry.TABLE_NAME, null, values);
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
