package io.coachapps.collegebasketballcoach.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import io.coachapps.collegebasketballcoach.R;
import io.coachapps.collegebasketballcoach.basketballsim.Simulator;
import io.coachapps.collegebasketballcoach.basketballsim.Team;

/**
 * DialogFragment used to display results of several games as they are being simmed.
 * Created by jojones on 10/24/16.
 */

public class GameResultsFragment extends DialogFragment {

    public class SimInfo {
        int year;
        int gameNum;
        List<Team> teamList;
        Simulator bballSim;
    }

    SimInfo info;

    public static GameResultsFragment newInstance(SimInfo info) {
        GameResultsFragment frag = new GameResultsFragment();
        frag.setInfo(info);
        return frag;
    }

    public void setInfo(SimInfo info) {
        this.info = info;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.player_information, container, false);

        return view;
    }
}
