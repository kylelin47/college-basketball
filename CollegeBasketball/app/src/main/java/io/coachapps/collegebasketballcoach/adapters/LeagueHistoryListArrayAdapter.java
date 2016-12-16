package io.coachapps.collegebasketballcoach.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import io.coachapps.collegebasketballcoach.R;
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

    public LeagueHistoryListArrayAdapter(Context context, List<LeagueResults> values) {
        super(context, R.layout.league_history_list_item, values);
        this.context = context;
        this.values = values;
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

        textViewTop.setText(values.get(position).year + ":");
        textViewMiddle.setText("Champion: " + values.get(position).championTeamName);
        textViewBottom.setText("MVP: " + playerDao.getPlayer(values.get(position).mvpId).name);

        return rowView;
    }
}
