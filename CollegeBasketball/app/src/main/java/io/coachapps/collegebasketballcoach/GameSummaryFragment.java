package io.coachapps.collegebasketballcoach;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.coachapps.collegebasketballcoach.adapters.PlayerBoxScoreListArrayAdapter;
import io.coachapps.collegebasketballcoach.basketballsim.Player;
import io.coachapps.collegebasketballcoach.db.BoxScoreDao;
import io.coachapps.collegebasketballcoach.db.GameDao;
import io.coachapps.collegebasketballcoach.db.PlayerDao;
import io.coachapps.collegebasketballcoach.models.BoxScore;
import io.coachapps.collegebasketballcoach.models.GameModel;

/**
 * Dialog Fragment for showing the summary of a certain game.
 * Created by Achi Jones on 10/25/2016.
 */

public class GameSummaryFragment extends DialogFragment {

    class BoxScoreIDComp implements Comparator<BoxScore> {
        @Override
        public int compare( BoxScore a, BoxScore b ) {
            return b.playerId - a.playerId;
        }
    }

    class PlayerBoxScoreComp implements Comparator<PlayerBoxScore> {
        @Override
        public int compare( PlayerBoxScore a, PlayerBoxScore b ) {
            return b.boxScore.playerStats.points - a.boxScore.playerStats.points;
        }
    }

    public class PlayerBoxScore {
        public BoxScore boxScore;
        public Player player;
    }

    private static final String YEAR_KEY = "year";
    private static final String WEEK_KEY = "week";
    private static final String HOME_KEY = "home";
    private static final String AWAY_KEY = "away";

    private GameModel gameModel;
    private List<PlayerBoxScore> awayPlayerBoxScores;
    private List<PlayerBoxScore> homePlayerBoxScores;

    public static GameSummaryFragment newInstance(int year, int week, String homeTeam, String awayTeam) {
        GameSummaryFragment fragment = new GameSummaryFragment();
        Bundle args = new Bundle();
        args.putInt(YEAR_KEY, year);
        args.putInt(WEEK_KEY, week);
        args.putString(HOME_KEY, homeTeam);
        args.putString(AWAY_KEY, awayTeam);
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

        // Get GameModel
        GameDao gameDao = new GameDao(getActivity());
        gameModel = gameDao.getGame(year, week, homeTeamName, awayTeamName);

        try {
            // Get Home/Away box scores
            PlayerDao playerDao = new PlayerDao(getActivity());

            List<Player> awayPlayers = playerDao.getPlayers(awayTeamName);
            int[] awayPlayerIDs = new int[awayPlayers.size()];
            for (int i = 0; i < awayPlayerIDs.length; ++i) {
                awayPlayerIDs[i] = awayPlayers.get(i).getId();
            }

            List<Player> homePlayers = playerDao.getPlayers(homeTeamName);
            int[] homePlayerIDs = new int[homePlayers.size()];
            for (int i = 0; i < homePlayerIDs.length; ++i) {
                homePlayerIDs[i] = homePlayers.get(i).getId();
            }

            BoxScoreDao boxScoreDao = new BoxScoreDao(getActivity());
            List<BoxScore> awayBoxScores = boxScoreDao.getBoxScoresFromGame(year, week, awayPlayerIDs);
            List<BoxScore> homeBoxScores = boxScoreDao.getBoxScoresFromGame(year, week, homePlayerIDs);

            awayPlayerBoxScores = new ArrayList<>();
            for (int i = 0; i < awayPlayerIDs.length; ++i) {
                PlayerBoxScore pbs = new PlayerBoxScore();
                pbs.boxScore = awayBoxScores.get(i);
                pbs.player = awayPlayers.get(i);
                awayPlayerBoxScores.add(pbs);
            }
            Collections.sort(awayPlayerBoxScores, new PlayerBoxScoreComp());

            homePlayerBoxScores = new ArrayList<>();
            for (int i = 0; i < homePlayerIDs.length; ++i) {
                PlayerBoxScore pbs = new PlayerBoxScore();
                pbs.boxScore = homeBoxScores.get(i);
                pbs.player = homePlayers.get(i);
                homePlayerBoxScores.add(pbs);
            }
            Collections.sort(homePlayerBoxScores, new PlayerBoxScoreComp());

        } catch (Exception e) {
            // TODO: how to handle this?
        }
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
        textHomeScore.setText(String.valueOf(gameModel.homeStats.points));

        textAwayName.setText(gameModel.awayTeam);
        textAwayWL.setText("");
        textAwayScore.setText(String.valueOf(gameModel.awayStats.points));

        final ListView listView = (ListView) view.findViewById(R.id.listViewGameSummary);
        Spinner spinner = (Spinner) view.findViewById(R.id.spinnerGameSummary);
        final List<String> list = new ArrayList<String>();
        list.add("Team Stats");
        list.add(gameModel.awayTeam + " Stats");
        list.add(gameModel.homeTeam + " Stats");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    // Team Stats
                } else if (i == 1) {
                    // Away Stats
                    listView.setAdapter(new PlayerBoxScoreListArrayAdapter(getActivity(), awayPlayerBoxScores));
                } else if (i == 2) {
                    // Home Stats
                    listView.setAdapter(new PlayerBoxScoreListArrayAdapter(getActivity(), homePlayerBoxScores));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return view;
    }

}

