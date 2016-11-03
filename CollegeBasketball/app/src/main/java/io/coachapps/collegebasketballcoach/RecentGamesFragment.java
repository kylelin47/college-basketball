package io.coachapps.collegebasketballcoach;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import io.coachapps.collegebasketballcoach.adapters.PlayerBoxScoreListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.RecentGamesListArrayAdapter;
import io.coachapps.collegebasketballcoach.db.BoxScoreDao;
import io.coachapps.collegebasketballcoach.models.BoxScore;

/**
 * Fragment for displaying the recent games of a particular player.
 * Created by jojones on 11/1/16.
 */

public class RecentGamesFragment extends Fragment {
    private static final String ID_KEY = "playerId";
    private static final String YEAR_KEY = "year";

    public static RecentGamesFragment newInstance(int playerId, int year) {
        RecentGamesFragment fragment = new RecentGamesFragment();
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
        View view = inflater.inflate(R.layout.recent_games_fragment, container, false);

        BoxScoreDao dao = new BoxScoreDao(getActivity());
        List<BoxScore> recentGames = dao.getBoxScoresForPlayer(getYear(), getPlayerID());

        ListView listView = (ListView) view.findViewById(R.id.listViewRecentGames);
        listView.setAdapter(new RecentGamesListArrayAdapter(getActivity(), recentGames));

        return view;
    }

}
