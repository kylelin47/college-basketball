package io.coachapps.collegebasketballcoach.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import io.coachapps.collegebasketballcoach.R;
import io.coachapps.collegebasketballcoach.basketballsim.Player;

/**
 * Array Adapter for displaying a player's rating in the player dialog.
 * Created by jojones on 9/14/16.
 */
public class PlayerRatingsListArrayAdapter  extends ArrayAdapter<String> {
    private final Context context;
    public final ArrayList<String> ratings;

    public PlayerRatingsListArrayAdapter(Context context, ArrayList<String> values) {
        super(context, R.layout.rating_list_item, values);
        this.context = context;
        this.ratings = values;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.rating_list_item, parent, false);

        TextView[] views = new TextView[6];
        views[1] = (TextView) rowView.findViewById(R.id.textViewRat1);
        views[0] = (TextView) rowView.findViewById(R.id.textViewRat1label);
        views[3] = (TextView) rowView.findViewById(R.id.textViewRat2);
        views[2] = (TextView) rowView.findViewById(R.id.textViewRat2label);
        views[5] = (TextView) rowView.findViewById(R.id.textViewRat3);
        views[4] = (TextView) rowView.findViewById(R.id.textViewRat3label);

        String[] ratingSplit = ratings.get(position).split(",");
        for (int i = 0; i < views.length; ++i) {
            views[i].setText(ratingSplit[i]);
        }

        return rowView;
    }

}