package io.coachapps.collegebasketballcoach.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import io.coachapps.collegebasketballcoach.R;
import io.coachapps.collegebasketballcoach.models.YearlyTeamStats;

/**
 * Adapter for displaying a team's history.
 * Created by jojones on 12/15/16.
 */

public class TeamHistoryListArrayAdapter extends ArrayAdapter<YearlyTeamStats> {
    private final Context context;
    private final List<YearlyTeamStats> values;

    public TeamHistoryListArrayAdapter(Context context, List<YearlyTeamStats> values) {
        super(context, R.layout.league_history_list_item, values);
        this.context = context;
        this.values = values;
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

        YearlyTeamStats stats = values.get(position);
        textViewTop.setText(stats.year + ":");
        textViewMiddle.setText(stats.team + " (" + stats.wins + "-" + stats.losses + ")");
        textViewBottom.setText(stats.summary);

        return rowView;
    }
}
