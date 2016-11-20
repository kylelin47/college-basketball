package io.coachapps.collegebasketballcoach.fragments;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.coachapps.collegebasketballcoach.MainActivity;
import io.coachapps.collegebasketballcoach.R;
import io.coachapps.collegebasketballcoach.adapters.player.SetLineupListArrayAdapter;
import io.coachapps.collegebasketballcoach.basketballsim.Player;
import io.coachapps.collegebasketballcoach.db.PlayerDao;

/**
 * Fragment used to set lineup/minutes for the user team.
 * Created by Achi Jones on 11/4/2016.
 */

public class SetLineupFragment extends DialogFragment {
    private final static String TEAM_KEY = "team";
    private String userTeam;
    private List<Player> players;
    private PlayerDao playerDao;
    public int selectedIndex;

    public static SetLineupFragment newInstance(String team) {
        Bundle args = new Bundle();
        args.putString(TEAM_KEY, team);
        SetLineupFragment fragment = new SetLineupFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_DeviceDefault_Light);
        setRetainInstance(true);
        userTeam = getArguments().getString(TEAM_KEY);
        selectedIndex = -1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.simple_list, container, false);
        playerDao = new PlayerDao(getActivity());
        try {
            players = playerDao.getPlayers(userTeam);
            Collections.sort(players, new Comparator<Player>() {
                @Override
                public int compare(Player left, Player right) {
                    return right.getLineupPosition() < left.getLineupPosition() ?
                            1 : left.getLineupPosition() == right.getLineupPosition() ? 0 : -1;
                }
            });
        } catch (Exception e) {
            // whoops
        }

        ListView listView = (ListView) view.findViewById(R.id.listView);
        listView.setAdapter(new SetLineupListArrayAdapter(getActivity(), this, players));

        return view;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        // Add your code here
        MainActivity mainActivity = (MainActivity)getActivity();
        mainActivity.onResume();
    }

    public void swapPlayers(int a, int b) {
        if (a >= players.size() || a < 0 || b >= players.size() || b < 0 || a==b) {
            selectedIndex = -1;
            return;
        }
        players.get(a).setLineupPosition(b);
        players.get(b).setLineupPosition(a);
        playerDao.updatePlayerRatings(players.get(a).getId(),
                players.get(a).ratings);
        playerDao.updatePlayerRatings(players.get(b).getId(),
                players.get(b).ratings);
        Player temp = players.get(a);
        players.set(a, players.get(b));
        players.set(b, temp);

        int[] backUpMinutes = new int[5];
        for (int i = 0; i < 10; ++i) {
            if (i < 5) {
                backUpMinutes[i] = 40 - players.get(i).getLineupMinutes();
            } else {
                players.get(i).setLineupMinutes(backUpMinutes[i-5]);
            }
        }
        selectedIndex = -1;
    }
}
