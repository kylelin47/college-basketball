package io.coachapps.collegebasketballcoach.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import io.coachapps.collegebasketballcoach.R;
import io.coachapps.collegebasketballcoach.basketballsim.Player;
import io.coachapps.collegebasketballcoach.db.PlayerDao;
import io.coachapps.collegebasketballcoach.util.DataDisplayer;
import io.coachapps.collegebasketballcoach.util.LeagueRecords;

public class LeagueRecordsListArrayAdapter extends ArrayAdapter<LeagueRecords.Record> {
    private final Context context;
    private final List<LeagueRecords.Record> values;
    private final String playerTeamName;
    private final PlayerDao playerDao;

    public LeagueRecordsListArrayAdapter(Context context, List<LeagueRecords.Record> values, String playerTeamName) {
        super(context, R.layout.league_records_list_item, values);
        this.context = context;
        this.values = values;
        this.playerTeamName = playerTeamName;
        playerDao = new PlayerDao(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.league_records_list_item, parent, false);

        TextView textLeft = (TextView) rowView.findViewById(R.id.textTeamStatsLeft);
        TextView textCenter = (TextView) rowView.findViewById(R.id.textTeamStatsCenter);
        TextView textRight = (TextView) rowView.findViewById(R.id.textTeamStatsRight);

        LeagueRecords.Record r = values.get(position);
        if (r != null) {
            textLeft.setText(getRoundedNumber(r));
            textCenter.setText(r.getDescription());
            if (r.getDescription().contains("Team")) {
                textRight.setText(r.getHolder() + " (" + r.getYear() + ")");
                if (playerTeamName.equals(r.getHolder())) {
                    // User Team, highlight using accent color
                    textLeft.setTextColor(Color.parseColor("#DD5600"));
                    textCenter.setTextColor(Color.parseColor("#DD5600"));
                    textRight.setTextColor(Color.parseColor("#DD5600"));
                }
            } else {
                // Player
                Player player = playerDao.getPlayer(Integer.parseInt(r.getHolder()));
                textRight.setText(player.name + " (" + r.getYear() + ")");
                if (playerTeamName.equals(player.teamName)) {
                    // User Team, highlight using accent color
                    textLeft.setTextColor(Color.parseColor("#DD5600"));
                    textCenter.setTextColor(Color.parseColor("#DD5600"));
                    textRight.setTextColor(Color.parseColor("#DD5600"));
                }
            }
        } else {
            textLeft.setText(" ");
            textCenter.setText(" ");
            textRight.setText(" ");
        }

        return rowView;
    }

    public String getRoundedNumber(LeagueRecords.Record record) {
        if (record.getDescription().contains("Team") || record.getDescription().contains("Percentage")) {
            return String.valueOf(DataDisplayer.round(record.getNumber(), 1));
        } else {
            return String.valueOf((int)record.getNumber());
        }
    }

    public LeagueRecords.Record getItem(int position) {
        return values.get(position);
    }
}
