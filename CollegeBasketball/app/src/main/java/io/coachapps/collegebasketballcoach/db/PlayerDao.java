package io.coachapps.collegebasketballcoach.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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
                Schemas.PlayerEntry.YEAR,
                Schemas.PlayerEntry.TEAM
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
                players.add(p);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return players;
    }

    public List<Player> getAllHoFPlayers() throws IOException, ClassNotFoundException {
        List<Player> players = new ArrayList<>();
        SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase();
        String[] projection = {
                Schemas.PlayerEntry._ID,
                Schemas.PlayerEntry.NAME,
                Schemas.PlayerEntry.RATINGS,
                Schemas.PlayerEntry.YEAR,
                Schemas.PlayerEntry.TEAM
        };
        String whereClause = Schemas.PlayerEntry.YEAR + " = 7";
        String orderBy = Schemas.PlayerEntry.TEAM + " ASC";
        try (Cursor c = db.query(Schemas.PlayerEntry.TABLE_NAME, projection, whereClause,
                null, null, null, orderBy, null)) {
            while (c.moveToNext()) {
                Player p = createPlayer(c);
                players.add(p);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return players;
    }

    public List<Player> getHoFPlayers(String teamName) throws IOException, ClassNotFoundException {
        List<Player> players = new ArrayList<>();
        SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase();
        String[] projection = {
                Schemas.PlayerEntry._ID,
                Schemas.PlayerEntry.NAME,
                Schemas.PlayerEntry.RATINGS,
                Schemas.PlayerEntry.YEAR,
                Schemas.PlayerEntry.TEAM
        };
        String whereClause = Schemas.PlayerEntry.YEAR + " = 7 AND " + Schemas.PlayerEntry.TEAM + " = ? ";
        String[] whereArgs = {
                teamName
        };
        String orderBy = Schemas.PlayerEntry.NAME + " ASC";
        try (Cursor c = db.query(Schemas.PlayerEntry.TABLE_NAME, projection, whereClause,
                whereArgs, null, null, orderBy, null)) {
            while (c.moveToNext()) {
                Player p = createPlayer(c);
                players.add(p);
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
                Schemas.PlayerEntry.YEAR,
                Schemas.PlayerEntry.TEAM
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
                }
            }
            values.put(Schemas.PlayerEntry.RATINGS, SerializationUtil.serialize(ratings));
            db.replaceOrThrow(Schemas.PlayerEntry.TABLE_NAME, null, values);
            db.setTransactionSuccessful();
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
        if (db.insert(Schemas.PlayerEntry.TABLE_NAME, null, values) == -1) {
            Log.i("PlayerDao", "No rows were inserted when inserting player " + player.id + " from " + player.team);
        } else {
            Log.i("PlayerDao", "Inserted " + player.id + " from " + player.team);
        }
    }

    private Player createPlayer(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas.PlayerEntry._ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(Schemas.PlayerEntry.NAME));
        String teamName = cursor.getString(cursor.getColumnIndexOrThrow(Schemas.PlayerEntry.TEAM));
        int year = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas.PlayerEntry.YEAR));
        PlayerRatings ratings = (PlayerRatings) SerializationUtil.deserialize(cursor.getBlob(cursor
                    .getColumnIndexOrThrow(Schemas.PlayerEntry.RATINGS)));
        return new Player(name, ratings, teamName, id, year);
    }
}
