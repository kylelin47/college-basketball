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

/**
 * Array Adapter for displaying player stats in the game dialog.
 * Created by Achi Jones on 10/3/2016.
 */
public class PlayerGameStatsListArrayAdapter extends ArrayAdapter<Player> {
    private final Context context;
    public final List<Player> players;

    public PlayerGameStatsListArrayAdapter(Context context, List<Player> values) {
        super(context, R.layout.game_stats_list_item, values);
        this.context = context;
        this.players = values;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.game_stats_list_item, parent, false);

        TextView playerName = (TextView) rowView.findViewById(R.id.textViewName);
        TextView playerPosition = (TextView) rowView.findViewById(R.id.textViewPosition);
        TextView playerOvrPot = (TextView) rowView.findViewById(R.id.textViewOvrPot);

        TextView playerPTS  = (TextView) rowView.findViewById(R.id.textViewPTS);
        TextView playerREB  = (TextView) rowView.findViewById(R.id.textViewREB);
        TextView playerAST  = (TextView) rowView.findViewById(R.id.textViewAST);
        TextView playerFGMA = (TextView) rowView.findViewById(R.id.textViewFGMA);
        TextView player3GMA = (TextView) rowView.findViewById(R.id.textView3GMA);
        TextView playerBLK  = (TextView) rowView.findViewById(R.id.textViewBLK);
        TextView playerSTL  = (TextView) rowView.findViewById(R.id.textViewSTL);

        Player p = players.get(position);

        playerName.setText(p.name);
        playerPosition.setText(Player.getPositionStr(p.getPosition()));
        playerOvrPot.setText(String.valueOf(p.getOverall()));

        playerPTS.setText(String.valueOf(p.gmStats.points));
        playerREB.setText(String.valueOf(p.gmStats.defensiveRebounds+p.gmStats.offensiveRebounds));
        playerAST.setText(String.valueOf(p.gmStats.assists));
        playerFGMA.setText(String.valueOf(p.gmStats.fieldGoalsMade)+"/"+String.valueOf(p.gmStats.fieldGoalsAttempted));
        player3GMA.setText(String.valueOf(p.gmStats.threePointsMade)+"/"+String.valueOf(p.gmStats.threePointsAttempted));
        playerBLK.setText(String.valueOf(p.gmStats.blocks));
        playerSTL.setText(String.valueOf(p.gmStats.steals));

        return rowView;
    }

    public Player getItem(int position) {
        return players.get(position);
    }

}
