package io.coachapps.collegebasketballcoach.adapters.player;

import android.content.Context;
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
 * Array Adapter for displaying players in the roster tab in MainActivity.
 * Created by Achi Jones on 9/14/2016.
 */
public class PlayerStatsListArrayAdapter extends ArrayAdapter<Player> {
    private final Context context;
    public final List<Player> players;
    private int currentYear;

    public PlayerStatsListArrayAdapter(Context context, List<Player> values, int currentYear) {
        super(context, R.layout.roster_list_item, values);
        this.context = context;
        this.players = values;
        this.currentYear = currentYear;
    }

    private static class ViewHolder {
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
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.roster_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.playerName = (TextView) convertView.findViewById(R.id.textViewName);
            viewHolder.playerPosition = (TextView) convertView.findViewById(R.id.textViewPosition);
            viewHolder.playerOvrPot = (TextView) convertView.findViewById(R.id.textViewOvrPot);

            viewHolder.playerPPG = (TextView) convertView.findViewById(R.id.textViewPPG);
            viewHolder.playerRPG = (TextView) convertView.findViewById(R.id.textViewRPG);
            viewHolder.playerAPG = (TextView) convertView.findViewById(R.id.textViewAPG);
            viewHolder.playerFGP = (TextView) convertView.findViewById(R.id.textViewFGP);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Player p = players.get(position);
        viewHolder.playerName.setText(p.name + " [" + DataDisplayer.getYearAbbreviation(p.year) +
                "]");
        if (position < 10) {
            viewHolder.playerPosition.setText(DataDisplayer.getPositionAbbreviation(position % 5 + 1));
        } else {
            viewHolder.playerPosition.setText("DNP");
        }
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
