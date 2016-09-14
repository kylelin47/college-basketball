package io.coachapps.collegebasketballcoach.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import io.coachapps.collegebasketballcoach.R;
import io.coachapps.collegebasketballcoach.basketballsim.Player;

/**
 * Array Adapter for displaying players in the roster tab in MainActivity.
 * Created by Achi Jones on 9/14/2016.
 */
public class PlayerStatsListArrayAdapter extends ArrayAdapter<Player> {
    private final Context context;
    public final ArrayList<Player> players;

    public PlayerStatsListArrayAdapter(Context context, ArrayList<Player> values) {
        super(context, R.layout.player_list_item, values);
        this.context = context;
        this.players = values;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.player_list_item, parent, false);

        TextView playerName = (TextView) rowView.findViewById(R.id.textViewName);
        TextView playerPosition = (TextView) rowView.findViewById(R.id.textViewPosition);
        TextView playerOvrPot = (TextView) rowView.findViewById(R.id.textViewOvrPot);

        TextView playerPPG = (TextView) rowView.findViewById(R.id.textViewPPG);
        TextView playerRPG = (TextView) rowView.findViewById(R.id.textViewRPG);
        TextView playerAPG = (TextView) rowView.findViewById(R.id.textViewAPG);

        Player p = players.get(position);

        playerName.setText(p.name);
        playerPosition.setText("" + p.getPosition());
        playerOvrPot.setText("" + p.getOverall());

        playerPPG.setText(String.valueOf(p.getPPG()));
        playerRPG.setText(String.valueOf(p.getRPG()));
        playerAPG.setText(String.valueOf(p.getAPG()));

        return rowView;
    }
}