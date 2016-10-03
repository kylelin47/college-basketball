package io.coachapps.collegebasketballcoach.basketballsim;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

import io.coachapps.collegebasketballcoach.adapters.PlayerGameStatsListArrayAdapter;

/**
 * Class to encapsulate viewing a game "as it happens".
 * Is responsible for running plays at a certain speed.
 * Created by Achi Jones on 9/30/2016.
 */

public class GameSimThread extends Thread {

    public static class GameDialogElements {
        public TextView textViewGameLog;
        public ScrollView scrollViewGameLog;
        public ListView listViewGameStats;
        public SeekBar seekBarGameSpeed;
        public Button buttonCallTimeout;

        public TextView textViewHomeAbbr;
        public TextView textViewAwayAbbr;
        public TextView textViewHomeScore;
        public TextView textViewAwayScore;
    }

    private Activity activity;
    private Context context;
    private PlayerGameStatsListArrayAdapter statsAdapter;
    private GameDialogElements uiElements;

    private StringBuilder gameLog;

    private Team home;
    private Team away;
    private ArrayList<Player> allPlayers;

    private int gameTime;
    private int maxGameTime;
    private int hscore;
    private int ascore;

    private int gameSpeed;

    private boolean poss_home;
    private boolean poss_away;
    private boolean playing;

    public GameSimThread(Activity activity, Context context, GameDialogElements uiElements, Team home, Team away) {
        this.activity = activity;
        this.context = context;

        this.uiElements = uiElements;

        this.home = home;
        this.away = away;

        allPlayers = new ArrayList<>();
        allPlayers.addAll(away.players);
        allPlayers.addAll(home.players);

        statsAdapter = new PlayerGameStatsListArrayAdapter(context, allPlayers);
        uiElements.listViewGameStats.setAdapter(statsAdapter);

        hscore = 0;
        ascore = 0;
        gameTime = 2400;
    }

    @Override
    public void run() {

        uiElements.textViewHomeAbbr.setText(home.getAbbr());
        uiElements.textViewAwayAbbr.setText(away.getAbbr());
        uiElements.textViewHomeScore.setText("0");
        uiElements.textViewAwayScore.setText("0");

        gameSpeed = 0;
        uiElements.seekBarGameSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Heh
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Heh
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                gameSpeed = uiElements.seekBarGameSpeed.getProgress();
            }
        });

        gameTime = 0;
        maxGameTime = 2400;

        poss_home = true;
        poss_away = false;

        playing = true;

        int home_tot_outd = home.getPG().getOutD() + home.getSG().getOutD() + home.getSF().getOutD() +
                home.getPF().getOutD() + home.getC().getOutD();
        int away_tot_outd = away.getPG().getOutD() + away.getSG().getOutD() + away.getSF().getOutD() +
                away.getPF().getOutD() + away.getC().getOutD();
        double hspeed = 6 - (home_tot_outd - away_tot_outd)/8;
        double aspeed = 6 - (away_tot_outd - home_tot_outd)/8;

        // Detect mismatches
        int[] matches_h = Simulator.detectMismatch(home, away);
        int[] matches_a = Simulator.detectMismatch(away, home);

        gameLog = new StringBuilder();

        while (playing) {

            if (poss_home) {
                hscore += Simulator.runPlay(home, away, matches_h, gameLog);
                poss_away = true;
                poss_home = false;
                gameTime += hspeed + 25 * Math.random();
                home.subPlayers( gameTime );
                matches_h = Simulator.detectMismatch(home, away);
            } else if (poss_away) {
                ascore += Simulator.runPlay(away, home, matches_a, gameLog);
                poss_away = false;
                poss_home = true;
                gameTime += aspeed + 25 * Math.random();
                away.subPlayers( gameTime );
                matches_a = Simulator.detectMismatch(away, home);
            }

            // Check if game has ended, or go to OT if needed
            if ( gameTime >= maxGameTime ) {
                gameTime = maxGameTime;
                if ( hscore != ascore ) {
                    playing = false;
                } else {
                    poss_home = true;
                    poss_away = false;
                    maxGameTime += 300;
                    gameLog.append("Tie game, advance to OVERTIME! ");
                }
            }

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // update TextView here!
                    String log = gameLog.toString();
                    gameLog.setLength(0);

                    // Update the score headings
                    uiElements.textViewHomeScore.setText(String.valueOf(hscore));
                    uiElements.textViewAwayScore.setText(String.valueOf(ascore));

                    // Add the game event to the game log
                    uiElements.textViewGameLog.append("Speed = " + gameSpeed + " " + getEventPrefix() + log + "\n\n");
                    uiElements.scrollViewGameLog.post(new Runnable() {
                        public void run()
                        {
                            uiElements.scrollViewGameLog.fullScroll(View.FOCUS_DOWN);
                        }
                    });

                    // Let the adapter know that values have changed
                    statsAdapter.notifyDataSetChanged();
                }
            });

            try {
                sleep(30 * (100 - gameSpeed) + 100);
            } catch (java.lang.InterruptedException e) {
                // uh
            }

        } // End playing loop
    }

    /**
     * Returns a string that is prefixed to any event in the game log.
     * Ex: 0:01 Q4 ALA 98 - 95 FLA
     * @return
     */
    private String getEventPrefix() {
        return "Time: " + (maxGameTime - gameTime) + " " + away.getAbbr() + " " + ascore + " - " + hscore + " " + home.getAbbr() + "\n";
    }

}
