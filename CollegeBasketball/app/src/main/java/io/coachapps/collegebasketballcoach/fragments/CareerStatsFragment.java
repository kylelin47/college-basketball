package io.coachapps.collegebasketballcoach.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import io.coachapps.collegebasketballcoach.R;
import io.coachapps.collegebasketballcoach.adapters.game.RecentGamesListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.player.CareerStatsListArrayAdapter;
import io.coachapps.collegebasketballcoach.basketballsim.Player;
import io.coachapps.collegebasketballcoach.db.BoxScoreDao;
import io.coachapps.collegebasketballcoach.db.PlayerDao;
import io.coachapps.collegebasketballcoach.db.YearlyPlayerStatsDao;
import io.coachapps.collegebasketballcoach.models.BoxScore;
import io.coachapps.collegebasketballcoach.models.YearlyPlayerStats;

/**
 * Fragment for displaying a players career statistics.
 * Created by jojones on 12/23/16.
 */

public class CareerStatsFragment extends Fragment {
    private static final String ID_KEY = "playerId";
    private static final String YEAR_KEY = "year";

    public static CareerStatsFragment newInstance(int playerId, int year) {
        CareerStatsFragment fragment = new CareerStatsFragment();
        Bundle args = new Bundle();
        args.putInt(ID_KEY, playerId);
        args.putInt(YEAR_KEY, year);
        fragment.setArguments(args);
        return fragment;
    }

    private int getPlayerID() {
        return getArguments().getInt(ID_KEY);
    }

    private int getYear() {
        return getArguments().getInt(YEAR_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.simple_list, container, false);

        YearlyPlayerStatsDao yearlyPlayerStatsDao = new YearlyPlayerStatsDao(getActivity());
        List<YearlyPlayerStats> careerStats = yearlyPlayerStatsDao.getPlayerStatsFromYears(
                getPlayerID(), 2016, getYear());

        PlayerDao playerDao = new PlayerDao(getActivity());
        Player player = playerDao.getPlayer(getPlayerID());

        ListView listViewStats = (ListView) view.findViewById(R.id.listView);
        listViewStats.setAdapter(new CareerStatsListArrayAdapter(getActivity(), careerStats, player));

        return view;
    }
}