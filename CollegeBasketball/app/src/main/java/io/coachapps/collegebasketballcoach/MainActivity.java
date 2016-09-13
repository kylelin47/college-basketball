package io.coachapps.collegebasketballcoach;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import io.coachapps.collegebasketballcoach.basketballsim.PlayerGen;
import io.coachapps.collegebasketballcoach.basketballsim.Simulator;
import io.coachapps.collegebasketballcoach.basketballsim.Team;

public class MainActivity extends AppCompatActivity {

    Simulator bballSim;
    PlayerGen playerGen;
    ArrayList<Team> teamList;

    Spinner teamSpinner;
    TextView currTeamTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Make generator, passing in possible player names
        playerGen = new PlayerGen(getString(R.string.league_player_names),
                                  getString(R.string.league_last_names));

        // Make 10 teams;
        teamList = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            teamList.add(new Team("Team " + i, playerGen));
        }

        // Sim games
        bballSim = new Simulator();
        bballSim.playSeason(teamList);

        // Set up UI components
        currTeamTextView = (TextView) findViewById(R.id.currentTeamText);

        teamSpinner = (Spinner) findViewById(R.id.examineTeamSpinner);
        ArrayList<String> teamStrList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            teamStrList.add(teamList.get(i).getName());
        }
        ArrayAdapter<String> dataAdapterTeam = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, teamStrList);
        dataAdapterTeam.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teamSpinner.setAdapter(dataAdapterTeam);
        teamSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        currTeamTextView.setText(teamList.get(position).getName() +
                                " Wins: " + teamList.get(position).getWins82());
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        //heh
                    }
                });


    }

}
