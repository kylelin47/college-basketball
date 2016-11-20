package io.coachapps.collegebasketballcoach.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import io.coachapps.collegebasketballcoach.R;
import io.coachapps.collegebasketballcoach.basketballsim.Player;
import io.coachapps.collegebasketballcoach.util.DataDisplayer;

public class PlayerDialogFragment extends DialogFragment {
    private Player player;
    private String teamName;
    private int year;
    private final static String PLAYER_KEY = "player";
    private final static String TEAM_KEY = "team";
    private final static String YEAR_KEY = "year";

    public static PlayerDialogFragment newInstance(Player player, String teamName, int year) {
        PlayerDialogFragment fragment = new PlayerDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(PLAYER_KEY, player);
        args.putString(TEAM_KEY, teamName);
        args.putInt(YEAR_KEY, year);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_DeviceDefault_Light);
        player = (Player) getArguments().getSerializable(PLAYER_KEY);
        teamName = getArguments().getString(TEAM_KEY);
        year = getArguments().getInt(YEAR_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.player_information, container, false);
        TextView textViewName = (TextView) view.findViewById(R.id.textViewName);
        TextView textViewTeamName = (TextView) view.findViewById(R.id.textViewTeamName);
        TextView textViewPrefPos = (TextView) view.findViewById(R.id.textViewPreferredPosition);
        TextView textViewPosition = (TextView) view.findViewById(R.id.textViewPosition);
        TextView textViewYear = (TextView) view.findViewById(R.id.textViewYear);
        TextView textViewOvrPot = (TextView) view.findViewById(R.id.textViewOvrPot);
        TextView textViewVitals = (TextView) view.findViewById(R.id.textViewVitals);
        Spinner spinner = (Spinner) view.findViewById(R.id.spinnerPlayerStats);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.player_display_choices, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private int previouslySelected = -1;
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != previouslySelected) {
                    if (i == 0) {
                        PlayerRatingsFragment playerRatingsFragment =
                                PlayerRatingsFragment.newInstance(player.ratings);
                        getChildFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                playerRatingsFragment).commit();
                    } else if (i == 1) {
                        PlayerStatsFragment playerStatsFragment =
                                PlayerStatsFragment.newInstance(player.getId(), year);
                        getChildFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                playerStatsFragment).commit();
                    } else if (i == 2) {
                        RecentGamesFragment recentGamesFragment =
                                RecentGamesFragment.newInstance(player.getId(), year);
                        getChildFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                recentGamesFragment).commit();
                    } else if (i == 3) {
                        PlayerAwardsFragment awardsFragment =
                                PlayerAwardsFragment.newInstance(
                                        player.getId(), teamName, year - player.year + 1, year);
                        getChildFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                awardsFragment).commit();
                    }
                }
                previouslySelected = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        textViewName.setText(player.name);
        textViewTeamName.setText(teamName);
        textViewPrefPos.setText("Preferred Pos: " +
                DataDisplayer.getPositionAbbreviation(player.getPosition()));
        textViewPosition.setText(player.getLineupPositionStr());
        textViewYear.setText(DataDisplayer.getYearAbbreviation(player.year));
        textViewOvrPot.setText(String.valueOf(player.getOverall()) + " / " +
                DataDisplayer.getLetterGrade(player.getPotential()));
        textViewVitals.setText(DataDisplayer.getHeight(player.ratings.heightInInches) + ", " +
                DataDisplayer.getWeight(player.ratings.weightInPounds));
        return view;
    }
}
