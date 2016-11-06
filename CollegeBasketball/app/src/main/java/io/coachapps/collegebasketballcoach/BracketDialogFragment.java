package io.coachapps.collegebasketballcoach;


import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import io.coachapps.collegebasketballcoach.adapters.BracketListArrayAdapter;
import io.coachapps.collegebasketballcoach.basketballsim.Game;

import static io.coachapps.collegebasketballcoach.R.id.bracketGameList;

public class BracketDialogFragment extends DialogFragment {
    private List<String> tournamentGames;
    private final static String GAMES_KEY = "games";

    public static BracketDialogFragment newInstance(List<Game> games) {
        BracketDialogFragment fragment = new BracketDialogFragment();
        Bundle args = new Bundle();
        ArrayList<String> tournamentGames = new ArrayList<>(games.size());
        for (Game game : games) {
            tournamentGames.add(game.toString());
        }
        args.putSerializable(GAMES_KEY, tournamentGames);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_DeviceDefault_Light);
        tournamentGames = (ArrayList<String>) getArguments().getSerializable(GAMES_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.bracket, container, false);
        ListView games = (ListView) view.findViewById(bracketGameList);
        BracketListArrayAdapter bracketListArrayAdapter = new BracketListArrayAdapter(getActivity(), this, tournamentGames);
        games.setAdapter(bracketListArrayAdapter);
        return view;
    }

    public void showGameSummaryDialog(int year, int week, String homeName, String awayName) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment newFragment = GameSummaryFragment.newInstance(
                year, week, homeName, awayName);
        newFragment.show(ft, "game dialog");
    }
}

