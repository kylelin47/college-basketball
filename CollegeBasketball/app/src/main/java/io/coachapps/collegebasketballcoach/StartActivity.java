package io.coachapps.collegebasketballcoach;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import io.coachapps.collegebasketballcoach.db.DbHelper;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("College Hoops Coach");

        ImageView imageLogo = (ImageView) findViewById(R.id.imageLogo);
        try {
            imageLogo.setImageResource(R.drawable.logo_small);
        } catch (OutOfMemoryError e) {
            Toast.makeText(StartActivity.this, "Error displaying logo",
                    Toast.LENGTH_SHORT).show();
        }

        Button newGameButton = (Button) findViewById(R.id.buttonNewGame);
        newGameButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                confirmNewGame();
            }
        });

        Button continueButton = (Button) findViewById(R.id.buttonContinue);
        continueButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                continueGame();
            }
        });

        Button rateAppButton = (Button) findViewById(R.id.buttonRate);
        rateAppButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=io.coachapps.collegebasketballcoach")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=io.coachapps.collegebasketballcoach")));
                }
            }
        });

        Button redditButton = (Button) findViewById(R.id.buttonReddit);
        redditButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("http://m.reddit.com/r/hoopscoach"));
                startActivity(intent);
            }
        });

        Button faqButton = (Button) findViewById(R.id.buttonFAQ);
        faqButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("http://m.reddit.com/r/hoopscoach/wiki/faq"));
                startActivity(intent);
            }
        });
    }

    public void confirmNewGame() {
        AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
        builder.setMessage("Are you sure you want to begin a new game? The existing save will be overwritten.")
                .setPositiveButton("Yes, Overwrite", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Actually overwrite
                        beginNewGame();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog2 = builder.create();
        dialog2.show();
    }

    public void beginNewGame() {
        File recruitingFile = new File(getFilesDir(), "current_state");
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(recruitingFile), "utf-8"))) {
            writer.write("SEASON");
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        SQLiteDatabase db = DbHelper.getInstance(StartActivity.this).getReadableDatabase();
        DbHelper.getInstance(StartActivity.this).resetDb(db);
        db.close();
        Intent myIntent = new Intent(StartActivity.this, MainActivity.class);
        StartActivity.this.startActivity(myIntent);
    }

    public void continueGame() {
        try {
            File stateFile = new File(getFilesDir(), "current_state");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(stateFile));
            String state = bufferedReader.readLine();
            System.out.println("Application State = " + state);
            if (state.equals("RECRUITING")) {
                Intent myIntent = new Intent(StartActivity.this, RecruitingActivity.class);
                StartActivity.this.startActivity(myIntent);
            } else {
                Intent myIntent = new Intent(StartActivity.this, MainActivity.class);
                StartActivity.this.startActivity(myIntent);
            }
        } catch (IOException e) {
            System.out.println("Error reading state file!");
            Intent myIntent = new Intent(StartActivity.this, MainActivity.class);
            StartActivity.this.startActivity(myIntent);
        }
    }

}
