package io.coachapps.collegebasketballcoach;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.coachapps.collegebasketballcoach.db.GameDao;
import io.coachapps.collegebasketballcoach.models.GameModel;

/**
 * Dialog Fragment for showing the summary of a certain game.
 * Created by Achi Jones on 10/25/2016.
 */

public class GameSummaryFragment extends DialogFragment {

    private static final String YEAR_KEY = "year";
    private static final String WEEK_KEY = "week";
    private static final String HOME_KEY = "home";
    private static final String AWAY_KEY = "away";

    private GameModel gameModel;

    public static GameSummaryFragment newInstance(int year, int week, String homeTeam, String awayTeam) {
        GameSummaryFragment fragment = new GameSummaryFragment();
        Bundle args = new Bundle();
        args.putInt(YEAR_KEY, year);
        args.putInt(WEEK_KEY, week);
        args.putString(HOME_KEY, homeTeam);
        args.putString(YEAR_KEY, awayTeam);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_DeviceDefault_Light);
        int year = getArguments().getInt(YEAR_KEY);
        int week = getArguments().getInt(WEEK_KEY);
        String homeTeamName = getArguments().getString(HOME_KEY);
        String awayTeamName = getArguments().getString(AWAY_KEY);
        GameDao gameDao = new GameDao(getActivity());
        gameModel = gameDao.getGame(year, week, homeTeamName, awayTeamName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.game_summary, container, false);

        TextView textHomeName = (TextView) view.findViewById(R.id.textViewHomeTeamName);
        TextView textHomeWL = (TextView) view.findViewById(R.id.textViewHomeTeamWL);
        TextView textHomeScore = (TextView) view.findViewById(R.id.textViewHomeTeamScore);

        TextView textAwayName = (TextView) view.findViewById(R.id.textViewAwayTeamName);
        TextView textAwayWL = (TextView) view.findViewById(R.id.textViewAwayTeamWL);
        TextView textAwayScore = (TextView) view.findViewById(R.id.textViewAwayTeamScore);

        textHomeName.setText(gameModel.homeTeam);
        textHomeWL.setText("");
        textHomeScore.setText(gameModel.homeStats.points);

        textAwayName.setText(gameModel.awayTeam);
        textAwayWL.setText("");
        textAwayScore.setText(gameModel.awayStats.points);

        return view;
    }

}
