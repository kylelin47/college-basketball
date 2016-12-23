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
 * Adapter for displaying a players career stats.
 * Created by jojones on 12/23/16.
 */

public class CareerStatsListArrayAdapter extends ArrayAdapter<YearlyPlayerStats> {
    private final Context context;
    private final List<YearlyPlayerStats> values;
    private final Player player;

    public CareerStatsListArrayAdapter(Context context, List<YearlyPlayerStats> values, Player player) {
        super(context, R.layout.roster_list_item, values);
        this.context = context;
        this.values = values;
        this.player = player;
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
        CareerStatsListArrayAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.roster_list_item, parent, false);
            viewHolder = new CareerStatsListArrayAdapter.ViewHolder();
            viewHolder.playerName = (TextView) convertView.findViewById(R.id.textViewName);
            viewHolder.playerPosition = (TextView) convertView.findViewById(R.id.textViewPosition);
            viewHolder.playerOvrPot = (TextView) convertView.findViewById(R.id.textViewOvrPot);

            viewHolder.playerPPG = (TextView) convertView.findViewById(R.id.textViewPPG);
            viewHolder.playerRPG = (TextView) convertView.findViewById(R.id.textViewRPG);
            viewHolder.playerAPG = (TextView) convertView.findViewById(R.id.textViewAPG);
            viewHolder.playerFGP = (TextView) convertView.findViewById(R.id.textViewFGP);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (CareerStatsListArrayAdapter.ViewHolder) convertView.getTag();
        }

        Player p = player;
        viewHolder.playerName.setText(p.name);
        viewHolder.playerPosition.setText(DataDisplayer.getPositionAbbreviation(p.getPosition()));
        viewHolder.playerOvrPot.setText(String.valueOf(values.get(position).year));

        YearlyPlayerStats currentStats = values.get(position);
        viewHolder.playerPPG.setText(currentStats.getPGDisplay("PPG"));
        viewHolder.playerRPG.setText(currentStats.getPGDisplay("RPG"));
        viewHolder.playerAPG.setText(currentStats.getPGDisplay("APG"));
        viewHolder.playerFGP.setText(currentStats.getPGDisplay("FG%")+
                "/"+currentStats.getPGDisplay("3P%"));

        return convertView;
    }
}
