package io.coachapps.collegebasketballcoach.adapters.recruiting;

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
import io.coachapps.collegebasketballcoach.util.DataDisplayer;

/**
 * List array adapter for displaying recruits during RecruitingActivity
 * Created by jojones on 11/13/16.
 */

public class RecruitsListArrayAdapter extends ArrayAdapter<Player> {

    private final Context context;
    private final List<Player> players;
    private final HashMap<Player, Integer> playerCostMap;
    private final HashMap<Player, String> playerPersonaliltyMap;

    public RecruitsListArrayAdapter(Context context, List<Player> players,
                                    HashMap<Player, Integer> playerCostMap,
                                    HashMap<Player, String> playerPersonaliltyMap) {
        super(context, R.layout.recruit_list_item, players);
        this.context = context;
        this.players = players;
        this.playerCostMap = playerCostMap;
        this.playerPersonaliltyMap = playerPersonaliltyMap;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(
                    getContext()).inflate(R.layout.recruit_list_item, parent, false);
        }

        TextView playerName = (TextView) convertView.findViewById(R.id.textViewName);
        TextView playerPosition = (TextView) convertView.findViewById(R.id.textViewPosition);
        TextView playerOvrPot = (TextView) convertView.findViewById(R.id.textViewOvrPot);

        Player p = players.get(position);
        playerName.setText(p.name + " [" + DataDisplayer.getYearAbbreviation(p.year) + "]");
        playerPosition.setText(DataDisplayer.getPositionAbbreviation(p.getPosition()));
        playerOvrPot.setText(String.valueOf(p.getOverall()) + " / " +
                DataDisplayer.getLetterGrade(p.getPotential()));

        TextView playerShooting    = (TextView) convertView.findViewById(R.id.textViewShooting);
        TextView playerDefense     = (TextView) convertView.findViewById(R.id.textViewDefense);
        TextView playerPassing     = (TextView) convertView.findViewById(R.id.textViewPassing);
        TextView playerRebounding  = (TextView) convertView.findViewById(R.id.textViewRebounding);
        TextView playerCost        = (TextView) convertView.findViewById(R.id.textViewCost);
        TextView playerPersonality = (TextView) convertView.findViewById(R.id.textViewPersonality);

        playerShooting.setText(DataDisplayer.getLetterGrade(p.getCompositeShooting()));
        playerDefense.setText(DataDisplayer.getLetterGrade(p.getCompositeDefense()));
        playerPassing.setText(DataDisplayer.getLetterGrade(p.getCompositePassing()));
        playerRebounding.setText(DataDisplayer.getLetterGrade(p.getCompositeRebounding()));
        playerCost.setText("$" + playerCostMap.get(p));
        playerPersonality.setText(playerPersonaliltyMap.get(p));

        DataDisplayer.colorizeRatings(playerShooting);
        DataDisplayer.colorizeRatings(playerDefense);
        DataDisplayer.colorizeRatings(playerPassing);
        DataDisplayer.colorizeRatings(playerRebounding);

        return convertView;
    }

    public Player getItem(int position) {
        return players.get(position);
    }
}
