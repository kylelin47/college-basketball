package io.coachapps.collegebasketballcoach.basketballsim;

import android.app.Activity;
import android.content.Context;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Class to encapsulate viewing a game "as it happens".
 * Is responsible for running plays at a certain speed.
 * Created by Achi Jones on 9/30/2016.
 */

public class GameSimThread extends Thread {

    public Activity activity;
    public Context context;

    public TextView textViewGameLog;
    public ScrollView scrollViewGameLog;

    public Team homeTeam;
    public Team awayTeam;

    public int gameTime;
    public int homeScore;
    public int awayScore;

    public int[] matches;

    public GameSimThread(Activity activity, Context context, TextView textViewGameLog, ScrollView scrollViewGameLog, Team homeTeam, Team awayTeam) {
        this.activity = activity;
        this.context = context;

        this.textViewGameLog = textViewGameLog;
        this.scrollViewGameLog = scrollViewGameLog;

        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;

        homeScore = 0;
        awayScore = 0;
        gameTime = 2400;
    }

    @Override
    public void run() {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // update TextView here!
                textViewGameLog.append("Wow glad everyone is having a good time!\n\n");
                scrollViewGameLog.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

}
