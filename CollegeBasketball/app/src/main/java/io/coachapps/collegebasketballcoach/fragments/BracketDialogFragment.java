package io.coachapps.collegebasketballcoach.fragments;


import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.coachapps.collegebasketballcoach.MainActivity;
import io.coachapps.collegebasketballcoach.R;
import io.coachapps.collegebasketballcoach.adapters.game.BracketListArrayAdapter;
import io.coachapps.collegebasketballcoach.basketballsim.Game;
import io.coachapps.collegebasketballcoach.basketballsim.League;

import static io.coachapps.collegebasketballcoach.R.id.bracketGameList;
import static io.coachapps.collegebasketballcoach.R.id.bracketTitle;

public class BracketDialogFragment extends DialogFragment {
    private List<String> tournamentGames;
    private String userTeamName;
    private final static String GAMES_KEY = "games";
    private final static String USER_TEAM_KEY = "userTeam";

    public static BracketDialogFragment newInstance(List<Game> games, String userTeamName) {
        BracketDialogFragment fragment = new BracketDialogFragment();
        Bundle args = new Bundle();
        ArrayList<String> tournamentGames = new ArrayList<>(games.size());
        for (Game game : games) {
            tournamentGames.add(game.toString());
        }
        args.putSerializable(GAMES_KEY, tournamentGames);
        args.putString(USER_TEAM_KEY, userTeamName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_DeviceDefault_Light);
        tournamentGames = (ArrayList<String>) getArguments().getSerializable(GAMES_KEY);
        userTeamName = getArguments().getString(USER_TEAM_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.bracket, container, false);
        TextView title = (TextView) view.findViewById(bracketTitle);
        String[] gameSummary = tournamentGames.get(1).split(",");
        if (gameSummary[7].equals(Game.GameType.MARCH_MADNESS.toString())) {
            title.setText(getResources().getString(R.string.march_madness_title));
        } else {
            title.setText(String.format(getResources().getString(R.string
                    .conference_tournament_title), League.Conference.valueOf(gameSummary[12])));
        }
        ListView games = (ListView) view.findViewById(bracketGameList);
        BracketListArrayAdapter bracketListArrayAdapter =
                new BracketListArrayAdapter(getActivity(), this, tournamentGames, userTeamName);
        games.setAdapter(bracketListArrayAdapter);
        return view;
    }

    public void showGameSummaryDialog(int year, int week, String homeName, String awayName,
                                      String homeNameSeed, String awayNameSeed) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment newFragment = GameSummaryFragment.newInstance(
                year, week, homeName, awayName, homeNameSeed, awayNameSeed);
        newFragment.show(ft, "game dialog");
    }
    public void examineTeam(String teamName) {
        ((MainActivity) getActivity()).examineTeam(teamName);
        dismiss();
    }
}

