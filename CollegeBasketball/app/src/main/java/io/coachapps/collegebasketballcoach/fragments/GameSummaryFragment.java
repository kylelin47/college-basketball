package io.coachapps.collegebasketballcoach.fragments;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.coachapps.collegebasketballcoach.MainActivity;
import io.coachapps.collegebasketballcoach.R;
import io.coachapps.collegebasketballcoach.adapters.player.PlayerBoxScoreListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.TeamStatsListArrayAdapter;
import io.coachapps.collegebasketballcoach.basketballsim.Player;
import io.coachapps.collegebasketballcoach.db.BoxScoreDao;
import io.coachapps.collegebasketballcoach.db.GameDao;
import io.coachapps.collegebasketballcoach.db.PlayerDao;
import io.coachapps.collegebasketballcoach.models.BoxScore;
import io.coachapps.collegebasketballcoach.models.GameModel;
import io.coachapps.collegebasketballcoach.util.DataDisplayer;

/**
 * Dialog Fragment for showing the summary of a certain game.
 * Created by Achi Jones on 10/25/2016.
 */

public class GameSummaryFragment extends DialogFragment {
    class BoxScoreComp implements Comparator<BoxScore> {
        @Override
        public int compare( BoxScore a, BoxScore b ) {
            return b.playerStats.points - a.playerStats.points;
        }
    }
    private static final String YEAR_KEY = "year";
    private static final String WEEK_KEY = "week";
    private static final String HOME_KEY = "home";
    private static final String AWAY_KEY = "away";
    private static final String HOME_WL_KEY = "homeWL";
    private static final String AWAY_WL_KEY = "awayWL";

    PlayerDao playerDao;
    private GameModel gameModel;
    private List<BoxScore> awayBoxScores;
    private List<BoxScore> homeBoxScores;

    public static GameSummaryFragment newInstance(int year, int week,
                                                  String homeTeam, String awayTeam,
                                                  String homeTeamWL, String awayTeamWL) {
        GameSummaryFragment fragment = new GameSummaryFragment();
        Bundle args = new Bundle();
        args.putInt(YEAR_KEY, year);
        args.putInt(WEEK_KEY, week);
        args.putString(HOME_KEY, homeTeam);
        args.putString(AWAY_KEY, awayTeam);
        args.putString(HOME_WL_KEY, homeTeamWL);
        args.putString(AWAY_WL_KEY, awayTeamWL);
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
        String homeWL = getArguments().getString(HOME_WL_KEY);
        String awayWL = getArguments().getString(AWAY_WL_KEY);
        playerDao = new PlayerDao(getActivity());
        // Get GameModel
        GameDao gameDao = new GameDao(getActivity());
        gameModel = gameDao.getGame(year, week, homeTeamName, awayTeamName);

        try {
            BoxScoreDao boxScoreDao = new BoxScoreDao(getActivity());
            awayBoxScores = boxScoreDao.getBoxScoresFromGame(year, week,
                    awayTeamName);
            homeBoxScores = boxScoreDao.getBoxScoresFromGame(year, week,
                    homeTeamName);
            Collections.sort(awayBoxScores, new BoxScoreComp());
            Collections.sort(homeBoxScores, new BoxScoreComp());

        } catch (Exception e) {
            // TODO: how to handle this?
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.game_summary, container, false);

        TextView textOT = (TextView) view.findViewById(R.id.textViewOT);
        if (gameModel.numOT == 0) {
            textOT.setVisibility(View.GONE);
        } else {
            if (gameModel.numOT == 1) {
                textOT.setText("OT");
            } else {
                textOT.setText(gameModel.numOT + "OT");
            }
        }

        TextView textHomeName = (TextView) view.findViewById(R.id.textViewHomeTeamName);
        TextView textHomeWL = (TextView) view.findViewById(R.id.textViewHomeTeamWL);
        TextView textHomeScore = (TextView) view.findViewById(R.id.textViewHomeTeamScore);

        TextView textAwayName = (TextView) view.findViewById(R.id.textViewAwayTeamName);
        TextView textAwayWL = (TextView) view.findViewById(R.id.textViewAwayTeamWL);
        TextView textAwayScore = (TextView) view.findViewById(R.id.textViewAwayTeamScore);

        textHomeName.setText(getArguments().getString(HOME_WL_KEY));
        textHomeWL.setText("");
        textHomeScore.setText(String.valueOf(gameModel.homeStats.stats.points));

        textAwayName.setText(getArguments().getString(AWAY_WL_KEY));
        textAwayWL.setText("");
        textAwayScore.setText(String.valueOf(gameModel.awayStats.stats.points));

        final ListView listView = (ListView) view.findViewById(R.id.listViewGameSummary);
        Spinner spinner = (Spinner) view.findViewById(R.id.spinnerGameSummary);
        final List<String> list = new ArrayList<String>();
        list.add("Team Stats Comparison");
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
                    listView.setAdapter(getTeamStatsAdapter());
                } else if (i == 1) {
                    // Away Stats
                    listView.setAdapter(new PlayerBoxScoreListArrayAdapter(getActivity(), awayBoxScores));
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {
                            BoxScore bs = (BoxScore)listView.getItemAtPosition(position);
                            Player p = playerDao.getPlayer(bs.playerId);
                            MainActivity mainActivity = (MainActivity) getActivity();
                            mainActivity.showPlayerDialog(p);
                        }
                    });
                } else if (i == 2) {
                    // Home Stats
                    listView.setAdapter(new PlayerBoxScoreListArrayAdapter(getActivity(), homeBoxScores));
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {
                            BoxScore bs = (BoxScore)listView.getItemAtPosition(position);
                            Player p = playerDao.getPlayer(bs.playerId);
                            MainActivity mainActivity = (MainActivity) getActivity();
                            mainActivity.showPlayerDialog(p);
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return view;
    }

    private TeamStatsListArrayAdapter getTeamStatsAdapter() {
        ArrayList<String> teamStatsList = new ArrayList<>();
        teamStatsList.add(gameModel.awayTeam + ">@>" + gameModel.homeTeam);
        teamStatsList.add(gameModel.awayStats.stats.points + ">Points>" + gameModel.homeStats.stats.points);
        teamStatsList.add((gameModel.awayStats.stats.defensiveRebounds+gameModel.awayStats.stats.offensiveRebounds) +
                ">Rebounds>" + (gameModel.homeStats.stats.defensiveRebounds+gameModel.homeStats.stats.offensiveRebounds));
        teamStatsList.add(gameModel.awayStats.stats.assists + ">Assists>" + gameModel.homeStats.stats.assists);
        teamStatsList.add(gameModel.awayStats.stats.steals + ">Steals>" + gameModel.homeStats.stats.steals);
        teamStatsList.add(gameModel.awayStats.stats.blocks + ">Blocks>" + gameModel.homeStats.stats.blocks);
        teamStatsList.add(gameModel.awayStats.stats.turnovers + ">Turnovers>" + gameModel.homeStats.stats.turnovers);

        teamStatsList.add(gameModel.awayStats.stats.fieldGoalsMade + "/" + gameModel.awayStats.stats.fieldGoalsAttempted +
                ">FGM/FGA>" + gameModel.homeStats.stats.fieldGoalsMade + "/" + gameModel.homeStats.stats.fieldGoalsAttempted);
        String awayFGP = DataDisplayer.getFieldGoalPercentage(
                gameModel.awayStats.stats.fieldGoalsMade, gameModel.awayStats.stats.fieldGoalsAttempted);
        String homeFGP = DataDisplayer.getFieldGoalPercentage(
                gameModel.homeStats.stats.fieldGoalsMade, gameModel.homeStats.stats.fieldGoalsAttempted);
        teamStatsList.add(awayFGP + "%>FG%>" + homeFGP + "%");

        teamStatsList.add(gameModel.awayStats.stats.threePointsMade + "/" + gameModel.awayStats.stats.threePointsAttempted +
                ">3FGM/3FGA>" + gameModel.homeStats.stats.threePointsMade + "/" + gameModel.homeStats.stats.threePointsAttempted);
        String away3GP = DataDisplayer.getFieldGoalPercentage(
                gameModel.awayStats.stats.threePointsMade, gameModel.awayStats.stats.threePointsAttempted);
        String home3GP = DataDisplayer.getFieldGoalPercentage(
                gameModel.homeStats.stats.threePointsMade, gameModel.homeStats.stats.threePointsAttempted);
        teamStatsList.add(away3GP + "%>3FG%>" + home3GP + "%");

        return new TeamStatsListArrayAdapter(getActivity(), teamStatsList, true);
    }

}

