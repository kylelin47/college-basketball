package io.coachapps.collegebasketballcoach.fragments;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.coachapps.collegebasketballcoach.R;
import io.coachapps.collegebasketballcoach.models.PlayerRatings;
import io.coachapps.collegebasketballcoach.util.DataDisplayer;

public class PlayerRatingsFragment extends Fragment {
    private static final String RATINGS_KEY = "playerRatings";

    public static PlayerRatingsFragment newInstance(PlayerRatings playerRatings) {
        PlayerRatingsFragment fragment = new PlayerRatingsFragment();
        Bundle args = new Bundle();
        args.putSerializable(RATINGS_KEY, playerRatings);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.rating_fragment, container, false);
        fillRatings(getPlayerRatings(), view);
        return view;
    }

    private PlayerRatings getPlayerRatings() {
        return (PlayerRatings) getArguments().getSerializable(RATINGS_KEY);
    }

    private void fillRatings(PlayerRatings playerRatings, View view) {
        ((TextView) view.findViewById(R.id.insideShooting)).setText(String.valueOf(playerRatings
                .insideShooting));
        ((TextView) view.findViewById(R.id.midrangeShooting)).setText(String.valueOf
                (playerRatings.midrangeShooting));
        ((TextView) view.findViewById(R.id.outsideShooting)).setText(String.valueOf(playerRatings
                .outsideShooting));
        ((TextView) view.findViewById(R.id.passing)).setText(String.valueOf(playerRatings.passing));
        ((TextView) view.findViewById(R.id.handling)).setText(String.valueOf(playerRatings
                .handling));
        ((TextView) view.findViewById(R.id.steal)).setText(String.valueOf(playerRatings.steal));
        ((TextView) view.findViewById(R.id.block)).setText(String.valueOf(playerRatings.block));
        ((TextView) view.findViewById(R.id.insideDefense)).setText(String.valueOf(playerRatings
                .insideDefense));
        ((TextView) view.findViewById(R.id.perimeterDefense)).setText(String.valueOf
                (playerRatings.perimeterDefense));
        ((TextView) view.findViewById(R.id.rebounding)).setText(String.valueOf(playerRatings
                .rebounding));
        ((TextView) view.findViewById(R.id.usage)).setText(String.valueOf(playerRatings.usage));
        ((TextView) view.findViewById(R.id.iq)).setText(String.valueOf(playerRatings.bballIQ));

        TextView offense = (TextView) view.findViewById(R.id.offenseLabel);
        offense.setText("Offense: " + DataDisplayer.getLetterGrade(playerRatings.getAggregateOffense()));
        DataDisplayer.colorizeRatings(offense);

        TextView fundamentals = (TextView) view.findViewById(R.id.fundamentalsLabel);
        fundamentals.setText("Fundamentals: " + DataDisplayer.getLetterGrade(playerRatings.getAggregateFundamentals()));
        DataDisplayer.colorizeRatings(fundamentals);

        TextView defense = (TextView) view.findViewById(R.id.defenseLabel);
        defense.setText("Defense: " + DataDisplayer.getLetterGrade(playerRatings.getAggregateDefense()));
        DataDisplayer.colorizeRatings(defense);
    }
}
