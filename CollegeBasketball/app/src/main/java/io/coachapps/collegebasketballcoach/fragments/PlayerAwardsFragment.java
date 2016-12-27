package io.coachapps.collegebasketballcoach.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import io.coachapps.collegebasketballcoach.MainActivity;
import io.coachapps.collegebasketballcoach.R;
import io.coachapps.collegebasketballcoach.adapters.player.PlayerAwardsListArrayAdapter;
import io.coachapps.collegebasketballcoach.db.LeagueResultsEntryDao;
import io.coachapps.collegebasketballcoach.models.LeagueResults;
import io.coachapps.collegebasketballcoach.util.DataDisplayer;

/**
 * Fragment for showing the awards that the player has received over his career.
 * Created by jojones on 11/19/16.
 */

public class PlayerAwardsFragment extends Fragment {
    private static final String ID_KEY = "playerId";
    private static final String TEAM_KEY = "team";
    private static final String BEGIN_YEAR_KEY = "beginYear";
    private static final String END_YEAR_KEY = "endYear";

    public static PlayerAwardsFragment newInstance(int playerId, String teamName,
                                                   int beginYear, int endYear) {
        PlayerAwardsFragment fragment = new PlayerAwardsFragment();
        Bundle args = new Bundle();
        args.putInt(ID_KEY, playerId);
        args.putString(TEAM_KEY, teamName);
        args.putInt(BEGIN_YEAR_KEY, beginYear);
        args.putInt(END_YEAR_KEY, endYear);
        fragment.setArguments(args);
        return fragment;
    }

    private int getPlayerID() {
        return getArguments().getInt(ID_KEY);
    }

    private String getTeam() {
        return getArguments().getString(TEAM_KEY);
    }

    private int getBeginYear() {
        return getArguments().getInt(BEGIN_YEAR_KEY);
    }

    private int getEndYear() {
        return getArguments().getInt(END_YEAR_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.simple_list, container, false);

        LeagueResultsEntryDao dao = new LeagueResultsEntryDao(getActivity());
        List<LeagueResults> leagueAwards = dao.getLeagueResults(getBeginYear(), getEndYear());

        List<String> playerAwardsCSV = new ArrayList<>();
        for (int i = 0; i < leagueAwards.size(); ++i) {
            StringBuilder sb = new StringBuilder();
            try {
                if (leagueAwards.get(i).championTeamName.equals(getTeam()))
                    sb.append("National Champion\n");
                if (leagueAwards.get(i).mvpId == getPlayerID()) sb.append("Player of the Year\n");
                if (leagueAwards.get(i).dpoyId == getPlayerID())
                    sb.append("Defensive Player of the Year\n");
                for (int t = 0; t < 3; ++t) {
                    for (int pos = 1; pos < 6; ++pos) {
                        if (leagueAwards.get(i).allAmericans.get(t).getIdPosition(pos) == getPlayerID()) {
                            sb.append(DataDisplayer.getRankStr(t + 1) + " Team All-American\n");
                        }
                        if (leagueAwards.get(i).allCowboy.get(t).getIdPosition(pos) == getPlayerID()) {
                            sb.append(DataDisplayer.getRankStr(t + 1) + " Team All-Cowboy\n");
                        } else if (leagueAwards.get(i).allLakes.get(t).getIdPosition(pos) == getPlayerID()) {
                            sb.append(DataDisplayer.getRankStr(t + 1) + " Team All-Lakes\n");
                        } else if (leagueAwards.get(i).allMountains.get(t).getIdPosition(pos) == getPlayerID()) {
                            sb.append(DataDisplayer.getRankStr(t + 1) + " Team All-Mountains\n");
                        } else if (leagueAwards.get(i).allNorth.get(t).getIdPosition(pos) == getPlayerID()) {
                            sb.append(DataDisplayer.getRankStr(t + 1) + " Team All-North\n");
                        } else if (leagueAwards.get(i).allPacific.get(t).getIdPosition(pos) == getPlayerID()) {
                            sb.append(DataDisplayer.getRankStr(t + 1) + " Team All-Pacific\n");
                        } else if (leagueAwards.get(i).allSouth.get(t).getIdPosition(pos) == getPlayerID()) {
                            sb.append(DataDisplayer.getRankStr(t + 1) + " Team All-South\n");
                        }
                    }
                }
                if (sb.length() != 0) {
                    playerAwardsCSV.add(leagueAwards.get(i).year + ":," + sb.toString().trim());
                } else if (getEndYear() == ((MainActivity)getActivity()).getYear()) {
                    playerAwardsCSV.add(leagueAwards.get(i).year + ":,None");
                }
            } catch (Exception e) {
                // Something went wrong...
            }
        }

        ListView listView = (ListView) view.findViewById(R.id.listView);
        listView.setAdapter(new PlayerAwardsListArrayAdapter(getActivity(), playerAwardsCSV));

        return view;
    }
}
