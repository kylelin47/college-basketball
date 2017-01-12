package io.coachapps.collegebasketballcoach.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.coachapps.collegebasketballcoach.basketballsim.Player;
import io.coachapps.collegebasketballcoach.basketballsim.PlayerGen;
import io.coachapps.collegebasketballcoach.basketballsim.Team;
import io.coachapps.collegebasketballcoach.models.PlayerModel;
import io.coachapps.collegebasketballcoach.util.SerializationUtil;

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
     */
    public void saveTeams(List<Team> teams, String playerTeamName) {
        PlayerDao playerDao = new PlayerDao(context);
        SQLiteDatabase db = DbHelper.getInstance(context).getWritableDatabase();
        db.beginTransaction();
        try {
            for (Team team : teams) {
                ContentValues values = new ContentValues();
                values.put(Schemas.TeamEntry.NAME, team.getName());
                values.put(Schemas.TeamEntry.CONFERENCE, team.conference);
                values.put(Schemas.TeamEntry.PRESTIGE, team.getPrestige());
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
    public List<Team> getAllTeams(int year, PlayerGen playerGen) throws IOException, ClassNotFoundException {
        // inner joins would give better performance
        playerGen.setCurrIDFillPosition(year);
        PlayerDao playerDao = new PlayerDao(context);
        List<Team> teams = new ArrayList<>();
        SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase();
        String[] projection = {
                Schemas.TeamEntry.NAME,
                Schemas.TeamEntry.PRESTIGE,
                Schemas.TeamEntry.IS_PLAYER,
                Schemas.TeamEntry.CONFERENCE
        };
        List<String> teamNames = new ArrayList<>();
        List<Integer> teamPrestiges = new ArrayList<>();
        List<Integer> isPlayerCheckers = new ArrayList<>();
        List<String> conferences = new ArrayList<>();
        db.beginTransaction();
        try {
            try (Cursor cursor = db.query(Schemas.TeamEntry.TABLE_NAME, projection, null, null,
                    null, null, null, null)) {
                while (cursor.moveToNext()) {
                    String teamName = cursor.getString(cursor.getColumnIndexOrThrow(Schemas
                            .TeamEntry.NAME));
                    int teamPrestige = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                            .TeamEntry.PRESTIGE));
                    int isPlayer = cursor.getInt(cursor.getColumnIndexOrThrow(Schemas
                            .TeamEntry.IS_PLAYER));
                    teamNames.add(teamName);
                    teamPrestiges.add(teamPrestige);
                    isPlayerCheckers.add(isPlayer);
                    conferences.add(cursor.getString(cursor.getColumnIndexOrThrow(Schemas
                            .TeamEntry.CONFERENCE)));
                }
            }
            for (int i = 0; i < teamNames.size(); ++i) {
                String teamName = teamNames.get(i);
                int teamPrestige = teamPrestiges.get(i);
                Team team = new Team(teamName, playerDao.getPlayers(teamName),
                        teamPrestige, isPlayerCheckers.get(i)==1, conferences.get(i));
                //System.out.println(team.getName() + " found with prestige = " + team.prestige);
                String[] teamProjection = {
                        Schemas.YearlyTeamStatsEntry.WINS,
                        Schemas.YearlyTeamStatsEntry.LOSSES
                };
                String whereClause = Schemas.YearlyTeamStatsEntry.TEAM + " = ? AND " + Schemas
                        .YearlyTeamStatsEntry.YEAR + " = ?";
                String[] whereArgs = {
                        teamName,
                        String.valueOf(year)
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
                fillPositions(team, playerGen, playerDao);
                teams.add(team);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return teams;
    }

    public void updateTeam(Team team) {
        SQLiteDatabase db = DbHelper.getInstance(context).getWritableDatabase();

        String whereClause = Schemas.TeamEntry.NAME + " = ?";
        String[] whereArgs = {
                team.getName()
        };

        ContentValues values = new ContentValues();
        values.put(Schemas.TeamEntry.PRESTIGE, team.prestige);
        int rows = db.update(Schemas.TeamEntry.TABLE_NAME, values, whereClause, whereArgs);
    }

    public void fillPositions(Team team, PlayerGen playerGen, PlayerDao playerDao) {
        for (int i = 1; i < 6; ++i) {
            int count = team.getPosTotals(i);
            while (count < 2) {
                Player p = playerGen.genPlayer(i, 50, 1);
                team.players.add(p);
                playerDao.save(new PlayerModel(p, team.getName()));
                count = team.getPosTotals(i);
            }
        }

        try {
            team.resetLineup();
        } catch (Exception e) {
            // lol
        }
    }
}
