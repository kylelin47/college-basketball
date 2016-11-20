package io.coachapps.collegebasketballcoach.adapters.player;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import io.coachapps.collegebasketballcoach.R;
import io.coachapps.collegebasketballcoach.basketballsim.Player;
import io.coachapps.collegebasketballcoach.basketballsim.Team;
import io.coachapps.collegebasketballcoach.db.YearlyPlayerStatsDao;
import io.coachapps.collegebasketballcoach.models.YearlyPlayerStats;
import io.coachapps.collegebasketballcoach.util.DataDisplayer;

/**
 * Adapter for displaying players in the end of season dialog.
 * Created by jojones on 11/18/16.
 */

public class PlayerAwardTeamListArrayAdapter extends ArrayAdapter<Player> {
    private final Context context;
    private final List<Player> players;
    private final HashMap<Integer, Team> playerTeamMap;
    private final int currentYear;

    public PlayerAwardTeamListArrayAdapter(Context context, List<Player> players,
                                           HashMap<Integer, Team> playerTeamMap, int year) {
        super(context, R.layout.award_team_list_item, players);
        this.context = context;
        this.players = players;
        this.playerTeamMap = playerTeamMap;
        this.currentYear = year;
    }

    private static class ViewHolder {
        TextView playerTeam;
        TextView playerName;
        TextView playerPosition;
        TextView playerOvrPot;

        TextView playerPPG;
        TextView playerRPG;
        TextView playerAPG;
        TextView playerFGP;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        PlayerAwardTeamListArrayAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.award_team_list_item, parent, false);
            viewHolder = new PlayerAwardTeamListArrayAdapter.ViewHolder();
            viewHolder.playerTeam = (TextView) convertView.findViewById(R.id.textViewTeam);
            viewHolder.playerName = (TextView) convertView.findViewById(R.id.textViewName);
            viewHolder.playerPosition = (TextView) convertView.findViewById(R.id.textViewPosition);
            viewHolder.playerOvrPot = (TextView) convertView.findViewById(R.id.textViewOvrPot);

            viewHolder.playerPPG = (TextView) convertView.findViewById(R.id.textViewPPG);
            viewHolder.playerRPG = (TextView) convertView.findViewById(R.id.textViewRPG);
            viewHolder.playerAPG = (TextView) convertView.findViewById(R.id.textViewAPG);
            viewHolder.playerFGP = (TextView) convertView.findViewById(R.id.textViewFGP);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (PlayerAwardTeamListArrayAdapter.ViewHolder) convertView.getTag();
        }

        Player p = players.get(position);
        viewHolder.playerTeam.setText(playerTeamMap.get(p.getId()).getRankNameWLStr());
        viewHolder.playerName.setText(p.name + " [" + DataDisplayer.getYearAbbreviation(p.year) +
                "]");
        String teamNumStr;
        if (position < 5) teamNumStr = "1st ";
        else if (position < 10) teamNumStr = "2nd ";
        else teamNumStr = "3rd ";
        viewHolder.playerPosition.setText(teamNumStr +
                DataDisplayer.getPositionAbbreviation(p.getLineupPosition() % 5 + 1));
        viewHolder.playerOvrPot.setText(String.valueOf(p.getOverall()) + " / " +
                DataDisplayer.getLetterGrade(p.getPotential()));

        YearlyPlayerStatsDao yearlyPlayerStatsDao = new YearlyPlayerStatsDao(getContext());
        List<YearlyPlayerStats> playerStats = yearlyPlayerStatsDao.getPlayerStatsFromYears(
                p.getId(), 2016, currentYear);
        if (playerStats.size() != 0 && playerStats.get(playerStats.size() - 1).year == currentYear) {
            YearlyPlayerStats currentStats = playerStats.get(playerStats.size() - 1);
            viewHolder.playerPPG.setText(currentStats.getPGDisplay("PPG"));
            viewHolder.playerRPG.setText(currentStats.getPGDisplay("RPG"));
            viewHolder.playerAPG.setText(currentStats.getPGDisplay("APG"));
            viewHolder.playerFGP.setText(currentStats.getPGDisplay("FG%")+
                    "/"+currentStats.getPGDisplay("3P%"));
        }

        return convertView;
    }

    public Player getItem(int position) {
        return players.get(position);
    }
}
