package io.coachapps.collegebasketballcoach.adapters.player;

import android.content.Context;
import android.graphics.Color;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import io.coachapps.collegebasketballcoach.R;
import io.coachapps.collegebasketballcoach.basketballsim.Player;
import io.coachapps.collegebasketballcoach.basketballsim.Team;
import io.coachapps.collegebasketballcoach.db.Schemas;
import io.coachapps.collegebasketballcoach.models.YearlyPlayerStats;
import io.coachapps.collegebasketballcoach.util.DataDisplayer;

/**
 * Array Adapter for displaying players in the league leaders dialog.
 * Created by Achi Jones on 9/14/2016.
 */
public class LeagueLeadersListArrayAdapter extends ArrayAdapter<Player> {
    private final Context context;
    public final List<Player> players;
    public final List<YearlyPlayerStats> stats;
    private final SparseArray<Team> playerTeamMap;
    private final String playerTeamName;
    private final String stat;

    public LeagueLeadersListArrayAdapter(Context context, List<Player> players,
                                         List<YearlyPlayerStats> stats, String stat,
                                         SparseArray<Team> playerTeamMap, String playerTeamName) {
        super(context, R.layout.league_leaders_list_item, players);
        this.context = context;
        this.players = players;
        this.stats = stats;
        this.playerTeamMap = playerTeamMap;
        this.playerTeamName = playerTeamName;
        this.stat = stat;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView;
        if (convertView == null) {
            rowView = inflater.inflate(R.layout.league_leaders_list_item, parent, false);
        } else {
            rowView = convertView;
        }

        TextView rank = (TextView) rowView.findViewById(R.id.textViewRank);
        TextView playerName = (TextView) rowView.findViewById(R.id.textViewName);
        TextView playerPosition = (TextView) rowView.findViewById(R.id.textViewPosition);
        TextView teamName = (TextView) rowView.findViewById(R.id.textViewTeamName);

        TextView statTotal = (TextView) rowView.findViewById(R.id.textViewTotal);
        TextView statPerGame = (TextView) rowView.findViewById(R.id.textViewPerGame);

        Player p = players.get(position);
        if (playerTeamMap.get(p.getId()).getName().equals(playerTeamName)) {
            playerName.setTextColor(Color.parseColor("#DD5600"));
            playerPosition.setTextColor(Color.parseColor("#DD5600"));
            teamName.setTextColor(Color.parseColor("#DD5600"));
        } else {
            playerName.setTextColor(Color.parseColor("#000000"));
            playerPosition.setTextColor(Color.parseColor("#888888"));
            teamName.setTextColor(Color.parseColor("#000000"));
        }

        playerName.setText(p.name + " [" + DataDisplayer.getYearAbbreviation(p.year) + "]");
        playerPosition.setText(p.getLineupPositionStr());
        teamName.setText(playerTeamMap.get(p.getId()).getRankNameWLStr());

        YearlyPlayerStats currentStats = stats.get(position);

        if (stat.equals(Schemas.YearlyPlayerStatsEntry.POINTS)) {
            statTotal.setText(String.valueOf(currentStats.playerStats.points));
            statPerGame.setText(String.valueOf(currentStats.getPGDisplay("PPG")));
        } else if (stat.equals(Schemas.YearlyPlayerStatsEntry.DEFENSIVE_REBOUNDS)) {
            statTotal.setText(String.valueOf(currentStats.playerStats.defensiveRebounds));
            statPerGame.setText(String.valueOf(currentStats.getPGDisplay("RPG")));
        } else if (stat.equals(Schemas.YearlyPlayerStatsEntry.ASSISTS)) {
            statTotal.setText(String.valueOf(currentStats.playerStats.assists));
            statPerGame.setText(String.valueOf(currentStats.getPGDisplay("APG")));
        } else if (stat.equals(Schemas.YearlyPlayerStatsEntry.BLOCKS)) {
            statTotal.setText(String.valueOf(currentStats.playerStats.blocks));
            statPerGame.setText(String.valueOf(currentStats.getPGDisplay("BPG")));
        } else if (stat.equals(Schemas.YearlyPlayerStatsEntry.STEALS)) {
            statTotal.setText(String.valueOf(currentStats.playerStats.steals));
            statPerGame.setText(String.valueOf(currentStats.getPGDisplay("SPG")));
        } else if (stat.equals(Schemas.YearlyPlayerStatsEntry.FGM)) {
            statTotal.setText(String.valueOf(currentStats.playerStats.fieldGoalsMade));
            statPerGame.setText(String.valueOf(currentStats.getPGDisplay("FGM")));
        } else if (stat.equals(Schemas.YearlyPlayerStatsEntry.THREE_POINTS_MADE)) {
            statTotal.setText(String.valueOf(currentStats.playerStats.threePointsMade));
            statPerGame.setText(String.valueOf(currentStats.getPGDisplay("3PM")));
        } else if (stat.equals(Schemas.YearlyPlayerStatsEntry.FTM)) {
            statTotal.setText(String.valueOf(currentStats.playerStats.freeThrowsMade));
            statPerGame.setText(String.valueOf(currentStats.getPGDisplay("FTM")));
        }

        rank.setText(String.valueOf(position+1));

        return rowView;
    }

    public Player getItem(int position) {
        return players.get(position);
    }

}
