package io.coachapps.collegebasketballcoach.adapters;

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
import io.coachapps.collegebasketballcoach.db.PlayerDao;
import io.coachapps.collegebasketballcoach.models.LeagueResults;

/**
 * Adapter for displaying the league's history.
 * Created by jojones on 12/15/16.
 */

public class LeagueHistoryListArrayAdapter extends ArrayAdapter<LeagueResults> {
    private final Context context;
    private final List<LeagueResults> values;
    private final PlayerDao playerDao;
    private final String playerTeamName;

    public LeagueHistoryListArrayAdapter(Context context, List<LeagueResults> values,
                                         String playerTeamName) {
        super(context, R.layout.league_history_list_item, values);
        this.context = context;
        this.values = values;
        this.playerTeamName = playerTeamName;
        this.playerDao = new PlayerDao(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView;
        if (convertView == null) {
            rowView = inflater.inflate(R.layout.league_history_list_item, parent, false);
        } else {
            rowView = convertView;
        }

        TextView textViewTop = (TextView) rowView.findViewById(R.id.textViewLeagueHistoryTop);
        TextView textViewMiddle = (TextView) rowView.findViewById(R.id.textViewLeagueHistoryMiddle);
        TextView textViewBottom = (TextView) rowView.findViewById(R.id.textViewLeagueHistoryBottom);

        Player mvp = playerDao.getPlayer(values.get(position).mvpId);

        textViewTop.setText(values.get(position).year + ":");
        textViewMiddle.setText("Champion: " + values.get(position).championTeamName);
        textViewBottom.setText("MVP: " + mvp.name + " (" + mvp.teamName + ")");

        if (values.get(position).championTeamName.equals(playerTeamName)) {
            textViewMiddle.setTextColor(Color.parseColor("#DD5600"));
        } else {
            textViewMiddle.setTextColor(Color.parseColor("#333333"));
        }

        if (mvp.teamName.equals(playerTeamName)) {
            textViewBottom.setTextColor(Color.parseColor("#DD5600"));
        } else {
            textViewBottom.setTextColor(Color.parseColor("#333333"));
        }

        return rowView;
    }
}
