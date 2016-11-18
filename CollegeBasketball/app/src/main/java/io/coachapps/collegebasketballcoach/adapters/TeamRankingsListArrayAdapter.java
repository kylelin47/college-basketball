package io.coachapps.collegebasketballcoach.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import io.coachapps.collegebasketballcoach.R;

/**
 * List adapter for the team rankings dialog
 * Created by jojones on 11/18/16.
 */

public class TeamRankingsListArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    public final ArrayList<String> values;

    public TeamRankingsListArrayAdapter(Context context, ArrayList<String> values) {
        super(context, R.layout.team_rankings_list_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.team_rankings_list_item, parent, false);

        TextView textLeft = (TextView) rowView.findViewById(R.id.textTeamRankingsLeft);
        TextView textCenter = (TextView) rowView.findViewById(R.id.textTeamRankingsCenter);
        TextView textRight = (TextView) rowView.findViewById(R.id.textTeamRankingsRight);

        String[] teamStat = values.get(position).split(",");
        textLeft.setText(teamStat[0]);
        textCenter.setText(teamStat[1]);
        textRight.setText(teamStat[2]);

        return rowView;
    }
}
