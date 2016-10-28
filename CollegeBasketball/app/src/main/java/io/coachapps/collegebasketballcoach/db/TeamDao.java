package io.coachapps.collegebasketballcoach.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.coachapps.collegebasketballcoach.basketballsim.Player;
import io.coachapps.collegebasketballcoach.basketballsim.Team;
import io.coachapps.collegebasketballcoach.models.PlayerModel;

public class TeamDao {
    private Context context;

    public TeamDao(Context context) {
        this.context = context;
    }

    public String getPlayerTeamName() {
        SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase();
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
        return null;
    }

    /**
     * Save teams and players in the teams. Should only be called once at very beginning of app
     * installation
     * @param teams
     */
    public void saveTeams(List<Team> teams, String playerTeamName) {
        PlayerDao playerDao = new PlayerDao(context);
        SQLiteDatabase db = DbHelper.getInstance(context).getWritableDatabase();
        db.beginTransaction();
        try {
            for (Team team : teams) {
                ContentValues values = new ContentValues();
                values.put(Schemas.TeamEntry.NAME, team.getName());
                values.put(Schemas.TeamEntry.CONFERENCE, "garbage");
                values.put(Schemas.TeamEntry.IS_PLAYER, team.getName().equals(playerTeamName));
                db.insert(Schemas.TeamEntry.TABLE_NAME, null, values);
                for (Player player : team.players) {
                    playerDao.save(new PlayerModel(player, team.getName()));
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Do this once at the beginning of the main activity
     * @return All teams and players found in the database. Empty list if none found
     */
    public List<Team> getAllTeams() throws IOException, ClassNotFoundException {
        // inner joins would give better performance
        PlayerDao playerDao = new PlayerDao(context);
        List<Team> teams = new ArrayList<>();
        SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase();
        String[] projection = {
                Schemas.TeamEntry.NAME,
        };
        List<String> teamNames = new ArrayList<>();
        db.beginTransaction();
        try {
            try (Cursor cursor = db.query(Schemas.TeamEntry.TABLE_NAME, projection, null, null,
                    null, null, null, null)) {
                while (cursor.moveToNext()) {
                    String teamName = cursor.getString(cursor.getColumnIndexOrThrow(Schemas
                            .TeamEntry.NAME));
                    teamNames.add(teamName);
                }
            }
            for (String teamName : teamNames) {
                Team team = new Team(teamName, playerDao.getPlayers(teamName));
                String[] teamProjection = {
                        Schemas.YearlyTeamStatsEntry.WINS,
                        Schemas.YearlyTeamStatsEntry.LOSSES
                };
                String whereClause = Schemas.YearlyTeamStatsEntry.TEAM + " = ?";
                String[] whereArgs = {
                        teamName
                };
                try (Cursor cursor = db.query(Schemas.YearlyTeamStatsEntry.TABLE_NAME,
                        teamProjection, whereClause, whereArgs, null, null, null, null)) {
                    if (cursor.moveToNext()) {
                        team.wins = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                                .YearlyTeamStatsEntry.WINS));
                        team.losses = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                                .YearlyTeamStatsEntry.LOSSES));
                    }
                }
                teams.add(team);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return teams;
    }
}
