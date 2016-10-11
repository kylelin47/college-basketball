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
import io.coachapps.collegebasketballcoach.util.DataDisplayer;


public class RosterListArrayAdapter extends ArrayAdapter<Player> {
    private final Context context;
    public final List<Player> players;
    public RosterListArrayAdapter(Context context, List<Player> values) {
        super(context, R.layout.roster, values);
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

        playerName.setText(p.name);
        playerPosition.setText(DataDisplayer.getPositionAbbreviation(p.getPosition()));
        playerOvrPot.setText(String.valueOf(p.getOverall()));

        playerPPG.setText(String.valueOf(p.getPPG()));
        playerRPG.setText(String.valueOf(p.getRPG()));
        playerAPG.setText(String.valueOf(p.getAPG()));
        playerFGP.setText(String.valueOf((int)p.getFGP())+"/"+String.valueOf((int)p.get3GP()));

        return rowView;
    }
}
