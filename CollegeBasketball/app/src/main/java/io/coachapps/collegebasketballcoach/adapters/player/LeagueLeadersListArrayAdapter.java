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

    public LeagueLeadersListArrayAdapter(Context context,
                                         List<Player> players, List<YearlyPlayerStats> stats) {
        super(context, R.layout.league_leaders_list_item, players);
        this.context = context;
        this.players = players;
        this.stats = stats;
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
        TextView playerOvrPot = (TextView) rowView.findViewById(R.id.textViewOvrPot);

        TextView playerPPG = (TextView) rowView.findViewById(R.id.textViewPPG);
        TextView playerRPG = (TextView) rowView.findViewById(R.id.textViewRPG);
        TextView playerAPG = (TextView) rowView.findViewById(R.id.textViewAPG);
        TextView playerFGP = (TextView) rowView.findViewById(R.id.textViewFGP);

        Player p = players.get(position);
        playerName.setText(p.name + " [" + DataDisplayer.getYearAbbreviation(p.year) + "]");
        playerPosition.setText(p.getLineupPositionStr());
        playerOvrPot.setText(String.valueOf(p.getOverall()) + " / " +
                DataDisplayer.getLetterGrade(p.getPotential()));

        YearlyPlayerStats currentStats = stats.get(position);
        playerPPG.setText(currentStats.getPGDisplay("PPG"));
        playerRPG.setText(currentStats.getPGDisplay("RPG"));
        playerAPG.setText(currentStats.getPGDisplay("APG"));
        playerFGP.setText(currentStats.getPGDisplay("FG%")+
                "/"+currentStats.getPGDisplay("3P%"));

        rank.setText(String.valueOf(position+1));

        return rowView;
    }

    public Player getItem(int position) {
        return players.get(position);
    }

}
