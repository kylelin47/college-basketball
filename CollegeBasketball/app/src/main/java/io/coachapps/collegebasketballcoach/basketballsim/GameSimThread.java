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
import java.util.List;

import io.coachapps.collegebasketballcoach.MainActivity;
import io.coachapps.collegebasketballcoach.adapters.PlayerGameStatsListArrayAdapter;
import io.coachapps.collegebasketballcoach.db.BoxScoreDao;
import io.coachapps.collegebasketballcoach.db.GameDao;
import io.coachapps.collegebasketballcoach.models.BoxScore;
import io.coachapps.collegebasketballcoach.models.GameModel;
import io.coachapps.collegebasketballcoach.models.Stats;
import io.coachapps.collegebasketballcoach.util.LeagueEvents;

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
    private StringBuilder gameEvents;

    private Game gm;
    private Team home;
    private Team away;
    private ArrayList<Player> allPlayers;
    private ArrayList<Player> homePlayers;
    private ArrayList<Player> awayPlayers;

    private int gameTime;
    private int maxGameTime;
    private int hscore;
    private int ascore;
    private int numOT;

    private int gameSpeed;

    private boolean poss_home;
    private boolean poss_away;
    private boolean playing;

    private volatile boolean isPaused;

    public GameSimThread(Activity activity, Context context, GameDialogElements uiElements, Game gm) {
        this.activity = activity;
        this.context = context;

        this.uiElements = uiElements;

        this.gm = gm;
        this.home = gm.getHome();
        this.away = gm.getAway();

        this.home.subPlayers(50);
        this.away.subPlayers(50);

        homePlayers = new ArrayList<>();
        homePlayers.addAll(home.players);

        awayPlayers = new ArrayList<>();
        awayPlayers.addAll(away.players);

        allPlayers = new ArrayList<>();
        allPlayers.addAll(away.players);
        allPlayers.addAll(home.players);

        statsAdapter = new PlayerGameStatsListArrayAdapter(context, awayPlayers);
        uiElements.listViewGameStats.setAdapter(statsAdapter);

        hscore = 0;
        ascore = 0;
        numOT = 0;
    }

    public void togglePause() {
        isPaused = !isPaused;
    }

    @Override
    public void run() {
        home.beginNewGame();
        away.beginNewGame();

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

        double playTime = 0;

        // Detect mismatches
        int[] matches_h;
        int[] matches_a;

        gameEvents = new StringBuilder();
        gameLog = new StringBuilder();

        while (playing) {
            if (!isPaused) {
                if (poss_home) {
                    matches_h = Simulator.detectMismatch(home, away);
                    hscore += Simulator.runPlay(home, away, matches_h, gameEvents);
                    poss_away = true;
                    poss_home = false;

                    playTime = hspeed + 20 * Math.random();
                } else if (poss_away) {
                    matches_a = Simulator.detectMismatch(away, home);
                    ascore += Simulator.runPlay(away, home, matches_a, gameEvents);
                    poss_away = false;
                    poss_home = true;

                    playTime = aspeed + 20 * Math.random();
                }

                gameTime += (int) playTime;
                away.addTimePlayed((int) playTime);
                home.addTimePlayed((int) playTime);
                if ((gameTime > 200 && Math.random() < 0.25) || (maxGameTime - gameTime < 120)) {
                    away.subPlayers(maxGameTime - gameTime);
                    home.subPlayers(maxGameTime - gameTime);
                }

                // Check if game has ended, or go to OT if needed
                if (gameTime >= maxGameTime) {
                    gameTime = maxGameTime;
                    if (hscore != ascore) {
                        playing = false;
                    } else {
                        poss_home = true;
                        poss_away = false;
                        maxGameTime += 300;
                        numOT++;
                        gameEvents.append("Tie game, advance to OVERTIME! ");
                    }
                }

                gameLog.append(getEventPrefix() + gameEvents.toString() + "\n\n");
                gameEvents.setLength(0);

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Update the score headings
                        uiElements.textViewHomeScore.setText(String.valueOf(hscore));
                        uiElements.textViewAwayScore.setText(String.valueOf(ascore));

                        // Add the game event to the game log
                        uiElements.textViewGameLog.append(gameLog.toString());
                        gameLog.setLength(0);
                        uiElements.scrollViewGameLog.post(new Runnable() {
                            public void run() {
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

            }

        } // End playing loop

        GameModel gameResult =
                LeagueEvents.saveGameResult(context, home, away, gm.getYear(), gm.getWeek());
        gm.setGameModel(gameResult);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Update the button to say done
                uiElements.buttonCallTimeout.setText("Done");
            }
        });

    }

    /**
     * Returns a string that is prefixed to any event in the game log.
     * Ex: 0:01 Q4 ALA 98 - 95 FLA
     * @return
     */
    private String getEventPrefix() {
        Team poss_team;
        // This is called after possession is flipped, so flip it back
        if (poss_home) poss_team = away;
        else poss_team = home;
        return convertTime() + "\t\t" + away.getAbbr() + " " + ascore + " - " + hscore + " " + home.getAbbr() + "\n" + poss_team.getAbbr() + " ball: ";
    }

    private String convertTime() {
        if (numOT > 0) {
            int minTime = (maxGameTime - gameTime) / 60;
            int secTime = (maxGameTime - gameTime) - 60 * minTime;
            if (secTime > 9)
                return minTime + ":" + secTime + " OT" + numOT;
            else
                return minTime + ":0" + secTime + " OT" + numOT;
        } else {
            int halfNum = (gameTime / 1200) + 1;
            if (halfNum > 2)
                return "0:00 H2";
            int minTime = (halfNum * (maxGameTime / 2) - gameTime) / 60;
            int secTime = (halfNum * (maxGameTime / 2) - gameTime) - 60 * minTime;
            if (secTime > 9)
                return minTime + ":" + secTime + " H" + halfNum;
            else
                return minTime + ":0" + secTime + " H" + halfNum;
        }
    }

    public boolean isPlaying() {
        return playing;
    }

    public void updateStatsAdapter(boolean homeOrAway) {
        if (homeOrAway) {
            // Home
            statsAdapter = new PlayerGameStatsListArrayAdapter(context, homePlayers);
            statsAdapter.notifyDataSetChanged();
            uiElements.listViewGameStats.setAdapter(statsAdapter);
        } else {
            // Away
            statsAdapter = new PlayerGameStatsListArrayAdapter(context, awayPlayers);
            statsAdapter.notifyDataSetChanged();
            uiElements.listViewGameStats.setAdapter(statsAdapter);
        }
    }

}
