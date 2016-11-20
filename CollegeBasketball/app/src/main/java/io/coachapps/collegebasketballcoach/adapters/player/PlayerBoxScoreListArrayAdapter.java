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
import io.coachapps.collegebasketballcoach.db.PlayerDao;
import io.coachapps.collegebasketballcoach.models.BoxScore;
import io.coachapps.collegebasketballcoach.models.Stats;
import io.coachapps.collegebasketballcoach.util.DataDisplayer;

/**
 * Adapter for game summaries.
 * Created by jojones on 10/25/16.
 */

public class PlayerBoxScoreListArrayAdapter extends ArrayAdapter<BoxScore> {

    private final Context context;
    public final List<BoxScore> playerBoxScores;
    private PlayerDao playerDao;

    public PlayerBoxScoreListArrayAdapter(Context context, List<BoxScore> values) {
        super(context, R.layout.game_stats_list_item, values);
        this.context = context;
        this.playerBoxScores = values;
        this.playerDao = new PlayerDao(context);
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

        TextView courtOrBench = (TextView) rowView.findViewById(R.id.textViewOnCourt);
        courtOrBench.setText("");

        BoxScore boxScore = playerBoxScores.get(position);
        Player player = playerDao.getPlayer(boxScore.playerId);
        Stats gmStats = boxScore.playerStats;
        playerName.setText(player.name);
        playerPosition.setText(DataDisplayer.getPositionAbbreviation(player.getLineupPosition()%5 + 1));
        playerOvrPot.setText(String.valueOf(gmStats.secondsPlayed/60) + "min");

        playerPTS.setText(String.valueOf(gmStats.points));
        playerREB.setText(String.valueOf(gmStats.defensiveRebounds+gmStats.offensiveRebounds));
        playerAST.setText(String.valueOf(gmStats.assists));
        playerFGMA.setText(String.valueOf(gmStats.fieldGoalsMade)+"/"+String.valueOf(gmStats.fieldGoalsAttempted));
        player3GMA.setText(String.valueOf(gmStats.threePointsMade)+"/"+String.valueOf(gmStats.threePointsAttempted));
        playerBLK.setText(String.valueOf(gmStats.blocks));
        playerSTL.setText(String.valueOf(gmStats.steals));

        return rowView;
    }

    public BoxScore getItem(int position) {
        return playerBoxScores.get(position);
    }

}
