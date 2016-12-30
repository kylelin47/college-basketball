package io.coachapps.collegebasketballcoach.adapters.recruiting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import io.coachapps.collegebasketballcoach.R;
import io.coachapps.collegebasketballcoach.RecruitingActivity;
import io.coachapps.collegebasketballcoach.basketballsim.Player;
import io.coachapps.collegebasketballcoach.basketballsim.Team;

/**
 * Adapter for displaying a list of commitments during recruiting
 * Created by jojones on 11/14/16.
 */

public class CommitmentsListArrayAdapter extends ArrayAdapter<RecruitingActivity.TeamPlayerCommitment> {
    private final Context context;
    private final List<RecruitingActivity.TeamPlayerCommitment> values;

    public CommitmentsListArrayAdapter(Context context, List<RecruitingActivity.TeamPlayerCommitment> values) {
        super(context, R.layout.commitment_list_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView;
        if (convertView == null) {
            rowView = inflater.inflate(R.layout.commitment_list_item, parent, false);
        } else {
            rowView = convertView;
        }

        TextView top = (TextView) rowView.findViewById(R.id.textViewTop);
        TextView bot = (TextView) rowView.findViewById(R.id.textViewBottom);

        Team t = values.get(position).team;
        Player p = values.get(position).player;
        top.setText(t.getName() + ", Prestige: " + t.getPrestige());
        bot.setText(p.toString());

        return rowView;
    }

    public RecruitingActivity.TeamPlayerCommitment getItem(int position) {
        return values.get(position);
    }
}
