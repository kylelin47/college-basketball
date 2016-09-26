package io.coachapps.collegebasketballcoach.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TeamDao {
    private Context context;

    public TeamDao(Context context) {
        this.context = context;
    }

    public String getPlayerTeamName() {
        try (SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase()) {
            String[] projection = {
                    Schemas.TeamEntry.NAME
            };
            String whereClause = Schemas.TeamEntry.IS_PLAYER + " = ?";
            String[] whereArgs = {
                    "1"
            };
            try (Cursor c = db.query(Schemas.TeamEntry.TABLE_NAME, projection, whereClause,
                    whereArgs, null, null, null, null)) {
                if (c.moveToNext()) {
                    return c.getString(c.getColumnIndexOrThrow(Schemas.TeamEntry.NAME));
                }
            }
        }
        return null;
    }
}
