package io.coachapps.collegebasketballcoach.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import io.coachapps.collegebasketballcoach.R;

/**
 * List adapter for displaying the player's awards on a year to year basis
 * Created by jojones on 11/19/16.
 */

public class PlayerAwardsListArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final List<String> values;

    public PlayerAwardsListArrayAdapter(Context context, List<String> values) {
        super(context, R.layout.player_awards_list_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.player_awards_list_item, parent, false);

        TextView textTop = (TextView) rowView.findViewById(R.id.textViewTop);
        TextView textBottom = (TextView) rowView.findViewById(R.id.textViewBottom);

        textTop.setText(values.get(position).split(",")[0]);
        textBottom.setText(values.get(position).split(",")[1]);

        return rowView;
    }
}
