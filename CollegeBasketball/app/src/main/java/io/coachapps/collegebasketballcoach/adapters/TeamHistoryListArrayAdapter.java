package io.coachapps.collegebasketballcoach.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
        if (stats != null) {
            textViewTop.setText(stats.year + ":");
            textViewMiddle.setText(stats.team + " (" + stats.wins + "-" + stats.losses + ")");
            textViewBottom.setText(stats.summary);
            if (stats.summary != null && stats.summary.contains("Won National Championship")) {
                textViewTop.setTextColor(Color.parseColor("#DD5600"));
            } else if (stats.summary != null && stats.summary.contains("Won Conference")) {
                textViewTop.setTextColor(Color.parseColor("#347378"));
            } else {
                textViewTop.setTextColor(Color.parseColor("#000000"));
            }
        } else {
            // null hack to display total stats
            int totalWins = 0;
            int totalLoss = 0;
            int totalCCs = 0;
            int totalNCs = 0;
            int totalFFs = 0;
            for (YearlyTeamStats yrStats : values) {
                if (yrStats != null) {
                    totalWins += yrStats.wins;
                    totalLoss += yrStats.losses;

                    if (yrStats.summary != null && yrStats.summary.contains("Won Conference")) {
                        totalCCs++;
                    }

                    if (yrStats.summary != null && yrStats.summary.contains("Won National Championship")) {
                        totalNCs++;
                        totalFFs++;
                    }
                    else if (yrStats.summary != null && yrStats.summary.contains("Made Final Four")) {
                        totalFFs++;
                    }
                }
            }
            textViewTop.setText("Total W-L: " + totalWins + "-" + totalLoss);
            textViewMiddle.setText("Conference Championships: " + totalCCs);
            textViewBottom.setText("Final Fours: " + totalFFs + "\nNational Championships: " + totalNCs);
            textViewTop.setTextColor(Color.parseColor("#000000"));
        }

        return rowView;
    }
}
