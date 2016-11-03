package io.coachapps.collegebasketballcoach.adapters;

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

    public PlayerStatsListArrayAdapter(Context context, List<Player> values) {
        super(context, R.layout.roster_list_item, values);
        this.context = context;
        this.players = values;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.roster_list_item, parent, false);

        TextView playerName = (TextView) rowView.findViewById(R.id.textViewName);
        TextView playerPosition = (TextView) rowView.findViewById(R.id.textViewPosition);
        TextView playerOvrPot = (TextView) rowView.findViewById(R.id.textViewOvrPot);

        TextView playerPPG = (TextView) rowView.findViewById(R.id.textViewPPG);
        TextView playerRPG = (TextView) rowView.findViewById(R.id.textViewRPG);
        TextView playerAPG = (TextView) rowView.findViewById(R.id.textViewAPG);
        TextView playerFGP = (TextView) rowView.findViewById(R.id.textViewFGP);

        Player p = players.get(position);
        playerName.setText(p.name + " [" + DataDisplayer.getYearAbbreviation(p.year) + "]");
        playerPosition.setText(DataDisplayer.getPositionAbbreviation(p.getPosition()));
        playerOvrPot.setText(String.valueOf(p.getOverall()) + " / " +
                DataDisplayer.getLetterGrade(p.getPotential()));

        YearlyPlayerStatsDao yearlyPlayerStatsDao = new YearlyPlayerStatsDao(getContext());
        List<YearlyPlayerStats> playerStats = yearlyPlayerStatsDao.getPlayerStatsFromYears(p
                .getId(), 2016, 3000);
        if (playerStats.size() != 0) {
            YearlyPlayerStats currentStats = playerStats.get(playerStats.size() - 1);
            playerPPG.setText(currentStats.getPGDisplay("PPG"));
            playerRPG.setText(currentStats.getPGDisplay("RPG"));
            playerAPG.setText(currentStats.getPGDisplay("APG"));
            playerFGP.setText(currentStats.getPGDisplay("FG%")+"/"+currentStats.getPGDisplay
                    ("3P%"));
        }

        return rowView;
    }

    public Player getItem(int position) {
        return players.get(position);
    }

}
