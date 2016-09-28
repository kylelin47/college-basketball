package io.coachapps.collegebasketballcoach.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import io.coachapps.collegebasketballcoach.models.PlayerModel;
import io.coachapps.collegebasketballcoach.models.PlayerRatings;

public class PlayerDao {
    private Context context;
    public PlayerDao(Context context) {
        this.context = context;
    }

    public List<PlayerModel> getPlayers(String teamName) throws IOException, ClassNotFoundException {
        List<PlayerModel> players = new ArrayList<>();
        try (SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase()) {
            String[] projection = {
                    Schemas.PlayerEntry._ID,
                    Schemas.PlayerEntry.NAME,
                    Schemas.PlayerEntry.RATINGS
            };
            String whereClause = Schemas.PlayerEntry.TEAM + " = ?";
            String[] whereArgs = {
                    teamName
            };
            try (Cursor c = db.query(Schemas.PlayerEntry.TABLE_NAME, projection, whereClause,
                    whereArgs, null, null, null, null)) {
                while (c.moveToNext()) {
                    players.add(createPlayer(c, teamName));
                }
            }
        }
        return players;
    }

    public void save(PlayerModel player) {
        try (SQLiteDatabase db = DbHelper.getInstance(context).getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(Schemas.PlayerEntry._ID, player.id);
            values.put(Schemas.PlayerEntry.NAME, player.name);
            values.put(Schemas.PlayerEntry.TEAM, player.team);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (ObjectOutput out = new ObjectOutputStream(bos)) {
                out.writeObject(player.ratings);
                out.flush();
                values.put(Schemas.PlayerEntry.RATINGS, bos.toByteArray());
            } catch (IOException e) {
                Log.e("PlayerDao", "Could not serialize PlayerRatings", e);
            }
            db.insert(Schemas.PlayerEntry.TABLE_NAME, null, values);
        }
    }

    private PlayerModel createPlayer(Cursor cursor, String teamName) throws IOException, ClassNotFoundException {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas.PlayerEntry._ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(Schemas.PlayerEntry.NAME));
        PlayerRatings ratings;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(cursor.getBlob(cursor
                .getColumnIndexOrThrow(Schemas.PlayerEntry.RATINGS))))) {
            ratings = (PlayerRatings) ois.readObject();
        }
        return new PlayerModel(id, name, teamName, ratings);
    }
}
