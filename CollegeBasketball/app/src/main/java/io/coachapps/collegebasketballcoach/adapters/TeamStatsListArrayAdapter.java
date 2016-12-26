package io.coachapps.collegebasketballcoach.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import io.coachapps.collegebasketballcoach.R;

/**
 * List adapter for displaying team stats and their respective values and ranks.
 * Values are passed in via CSV, ex: "123.4,Points Per Game,1st" would be a valid value
 * Created by jojones on 9/19/16.
 */
public class TeamStatsListArrayAdapter extends ArrayAdapter<String> {

    private static final int smallSize = 15;
    private static final int bigSize = 18;

    private final Context context;
    public final ArrayList<String> values;
    private boolean boldHigher;

    public TeamStatsListArrayAdapter(Context context, ArrayList<String> values, boolean boldHigher) {
        super(context, R.layout.team_stats_list_item, values);
        this.context = context;
        this.values = values;
        this.boldHigher = boldHigher;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.team_stats_list_item, parent, false);

        TextView textLeft = (TextView) rowView.findViewById(R.id.textTeamStatsLeft);
        TextView textCenter = (TextView) rowView.findViewById(R.id.textTeamStatsCenter);
        TextView textRight = (TextView) rowView.findViewById(R.id.textTeamStatsRight);

        String[] teamStat = values.get(position).split(",");
        textLeft.setText(teamStat[0]);
        textCenter.setText(teamStat[1]);
        textRight.setText(teamStat[2]);

        if (boldHigher) {
            try {
                double left = Double.parseDouble(teamStat[0]);
                double right = Double.parseDouble(teamStat[2]);
                if ((left > right && !teamStat[1].contains("Opp")) ||
                        (left < right && teamStat[1].contains("Opp"))) {
                    textLeft.setTextSize(TypedValue.COMPLEX_UNIT_SP, bigSize);
                    textRight.setTextSize(TypedValue.COMPLEX_UNIT_SP, smallSize);
                    textLeft.setTypeface(null, Typeface.BOLD);
                    textRight.setTypeface(null, Typeface.NORMAL);
                } else if ((right > left && !teamStat[1].contains("Opp")) ||
                        (right < left && teamStat[1].contains("Opp"))) {
                    textLeft.setTextSize(TypedValue.COMPLEX_UNIT_SP, smallSize);
                    textRight.setTextSize(TypedValue.COMPLEX_UNIT_SP, bigSize);
                    textLeft.setTypeface(null, Typeface.NORMAL);
                    textRight.setTypeface(null, Typeface.BOLD);
                } else {
                    textLeft.setTextSize(TypedValue.COMPLEX_UNIT_SP, smallSize);
                    textRight.setTextSize(TypedValue.COMPLEX_UNIT_SP, smallSize);
                    textLeft.setTypeface(null, Typeface.NORMAL);
                    textRight.setTypeface(null, Typeface.NORMAL);
                }
            } catch (Exception e) {
                // Couldn't parse
                textLeft.setTextSize(TypedValue.COMPLEX_UNIT_SP, smallSize);
                textRight.setTextSize(TypedValue.COMPLEX_UNIT_SP, smallSize);
                textLeft.setTypeface(null, Typeface.NORMAL);
                textRight.setTypeface(null, Typeface.NORMAL);
            }
        }

        return rowView;
    }
}
