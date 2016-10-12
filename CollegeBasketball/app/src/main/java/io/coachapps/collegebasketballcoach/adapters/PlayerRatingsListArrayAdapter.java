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
 * Array Adapter for displaying a player's rating in the player dialog.
 * Created by jojones on 9/14/16.
 */
public class PlayerRatingsListArrayAdapter  extends ArrayAdapter<String> {
    private final Context context;
    public final List<String> ratings;

    public PlayerRatingsListArrayAdapter(Context context, List<String> values) {
        super(context, R.layout.rating_list_item, values);
        this.context = context;
        this.ratings = values;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.rating_list_item, parent, false);

        TextView rating = (TextView) rowView.findViewById(R.id.textViewRat1);
        TextView label = (TextView) rowView.findViewById(R.id.textViewRat1label);

        String[] ratingSplit = ratings.get(position).split(",");
        // "Interior Shooting,20"
        label.setText(ratingSplit[0]);
        rating.setText(ratingSplit[1]);

        return rowView;
    }

}
