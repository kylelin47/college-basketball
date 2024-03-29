package io.coachapps.collegebasketballcoach.adapters.player;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import io.coachapps.collegebasketballcoach.R;
import io.coachapps.collegebasketballcoach.basketballsim.Player;
import io.coachapps.collegebasketballcoach.db.YearlyPlayerStatsDao;
import io.coachapps.collegebasketballcoach.models.YearlyPlayerStats;
import io.coachapps.collegebasketballcoach.util.DataDisplayer;

/**
 * Adapter for displaying players in the hall of fame.
 * Created by jojones on 12/23/16.
 */

public class HallOfFameListArrayAdapter extends ArrayAdapter<Player> {
    private final Context context;
    public final List<Player> players;
    private int currentYear;
    private String playerTeam;

    public HallOfFameListArrayAdapter(Context context, List<Player> values,
                                      int currentYear, String playerTeam) {
        super(context, R.layout.award_team_list_item, values);
        this.context = context;
        this.players = values;
        this.currentYear = currentYear;
        this.playerTeam = playerTeam;
    }

    private static class ViewHolder {
        TextView teamName;
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
        HallOfFameListArrayAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.award_team_list_item, parent, false);
            viewHolder = new HallOfFameListArrayAdapter.ViewHolder();
            viewHolder.teamName = (TextView) convertView.findViewById(R.id.textViewTeam);
            viewHolder.playerName = (TextView) convertView.findViewById(R.id.textViewName);
            viewHolder.playerPosition = (TextView) convertView.findViewById(R.id.textViewPosition);
            viewHolder.playerOvrPot = (TextView) convertView.findViewById(R.id.textViewOvrPot);

            viewHolder.playerPPG = (TextView) convertView.findViewById(R.id.textViewPPG);
            viewHolder.playerRPG = (TextView) convertView.findViewById(R.id.textViewRPG);
            viewHolder.playerAPG = (TextView) convertView.findViewById(R.id.textViewAPG);
            viewHolder.playerFGP = (TextView) convertView.findViewById(R.id.textViewFGP);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (HallOfFameListArrayAdapter.ViewHolder) convertView.getTag();
        }

        Player p = players.get(position);
        viewHolder.teamName.setText(p.teamName);
        if (playerTeam.equals(p.teamName)) {
            // Highlight user player
            viewHolder.teamName.setTextColor(Color.parseColor("#DD5600"));
        } else {
            viewHolder.teamName.setTextColor(Color.parseColor("#000000"));
        }
        viewHolder.playerName.setText(p.name + " [" + DataDisplayer.getYearAbbreviation(p.year) +
                "]");
        viewHolder.playerPosition.setText(DataDisplayer.getPositionAbbreviation(p.getPosition()));

        YearlyPlayerStatsDao yearlyPlayerStatsDao = new YearlyPlayerStatsDao(getContext());
        List<YearlyPlayerStats> playerStats = yearlyPlayerStatsDao.getPlayerStatsFromYears(
                p.getId(), 2016, currentYear);
        if (playerStats.size() != 0) {
            int totalPoints = 0;
            int totalRebounds = 0;
            int totalAssists = 0;
            int totalFGM = 0;
            int totalFGA = 0;
            int total3GM = 0;
            int total3GA = 0;
            int totalGames = 0;
            for (YearlyPlayerStats currentStats : playerStats) {
                totalPoints += currentStats.playerStats.points;
                totalRebounds += currentStats.playerStats.defensiveRebounds + currentStats.playerStats.offensiveRebounds;
                totalAssists += currentStats.playerStats.assists;
                totalFGM += currentStats.playerStats.fieldGoalsMade;
                totalFGA += currentStats.playerStats.fieldGoalsAttempted;
                total3GM += currentStats.playerStats.threePointsMade;
                total3GA += currentStats.playerStats.threePointsAttempted;
                if (currentStats.playerStats.points > 0) totalGames += currentStats.gamesPlayed;
            }
            if (totalFGA == 0) totalFGA = 1;
            if (total3GA == 0) total3GA = 1;
            viewHolder.playerPPG.setText(String.valueOf(DataDisplayer.round((double)totalPoints/totalGames, 1)));
            viewHolder.playerRPG.setText(String.valueOf(DataDisplayer.round((double)totalRebounds/totalGames, 1)));
            viewHolder.playerAPG.setText(String.valueOf(DataDisplayer.round((double)totalAssists/totalGames, 1)));
            viewHolder.playerFGP.setText((totalFGM*100)/totalFGA + "/" + (total3GM*100)/total3GA);

            viewHolder.playerOvrPot.setText(String.valueOf(playerStats.get(playerStats.size()-1).year));
        }

        return convertView;
    }

    public Player getItem(int position) {
        return players.get(position);
    }

}

